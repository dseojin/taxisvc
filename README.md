# CALL TAXI SERVICE

## 1. 아키텍처
### 1.1 MSA 아키텍처 구성도
```
1. gateway service를 통해 단일진입이 가능하도록 라우팅 설정되었다.
2. 택시를 요청하는 call 서비스, 결제가 이루어지는 payment 서비스, 드라이브 데이터를 관리하는 drive 서비스로 구성되어있고
call 서비스와 drive 서비스의 상세 모델을 참조하여 calldashboard(callView) 서비스를 만들어 CQRS를 적용하였다.
3. Istio 서비스 메쉬를 활용하여 모니터링 툴 그라파나를 사용하였다.
```
![image](https://github.com/dseojin/taxisvc/assets/173647509/9c2ed0d4-61b9-4555-8148-1c94bb9bd10f)


## 2. 모델링
### 2.1 이벤트 스토밍
```
- call
사용자가 택시를 호출한다.
택시 호출 시 결제가 이루어지고 결제 성공 시 운행을 요청한다.
결제 실패 시 호출 상태를 취소로 변경한다.
사용자가 택시호출을 취소한다.
호출 취소 시 결제를 취소한다.
호출 관련 데이터를 중간중간 조회한다.

- drive
결제가 완료된 호출 요청이 오면 운행이 시작된다.
운행 시작 시 호출 상태를 변경한다.
요청거리가 먼 경우 호출을 취소 시킨다.
드라이버가 운행을 종료한다.
운행 종료 시 호출 상태를 변경한다.
```
![image](https://github.com/dseojin/taxisvc/assets/173647509/606bc341-bb25-4f6c-b1bf-8f366cb32a7a)

## 3. 구현
```
# 서비스별 포트 참고
call : 8082
payment : 8083
drive : 8084
callView(CQRS) : 8085
gateway : 8088
```
### 3.1 분산트랜잭션
```
//마이크로 서비스간의 통신에서 이벤트 메세지를 Pub/Sub 하여 분산 트랜젝션을 구현하였다.
//payment 서비스에서 결제가 완료되면 Payment Aggregate에 insert가 발생하고
// @PostPersist를 통해 post 발생 시 'farePaid' 이벤트를 발행하도록 구현하였다.
// payment/src/main/java/taxisvc/domain/Payment.java
package taxisvc.domain;

@Entity
@Table(name = "Payment_table")
@Data
@SequenceGenerator(
  name = "PAYMENT_SEQ_GENERATOR", 
  sequenceName = "PAYMENT_SEQ", // 매핑할 데이터베이스 시퀀스 이름 
  initialValue = 1,
  allocationSize = 1)
//<<< DDD / Aggregate Root
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long payId;

    private Long callId;

    private BigDecimal fare;

    @PostPersist
    public void onPostPersist() {
        FarePaid farePaid = new FarePaid(this);
        farePaid.publishAfterCommit();
    }

// 'farePaid' 이벤트를 수신한 drive에서 policy를 통해 운행을 시작하는 로직을 구현하였다.
// drive/src/main/java/taxisvc/infra/PolicyHandler.java
package taxisvc.infra;

@Service
@Transactional
public class PolicyHandler {

    @Autowired
    DriveRepository driveRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void whatever(@Payload String eventString) {}

    @StreamListener(
        value = KafkaProcessor.INPUT,
        condition = "headers['type']=='FarePaid'"
    )
    public void wheneverFarePaid_RequestDriver(@Payload FarePaid farePaid) {
        // 요금지불 이벤트가 생성되면
        // 드라이브시작 로직 수행
        FarePaid event = farePaid;

        Drive.requestDriver(event);
    }
}

// 로직은 Drive Aggregate에 구현
//drive/src/main/java/taxisvc/domain/Drive.java
package taxisvc.domain;

@Entity
@Table(name = "Drive_table")
@Data
@SequenceGenerator(
  name = "DRIVE_SEQ_GENERATOR", 
  sequenceName = "DRIVE_SEQ", // 매핑할 데이터베이스 시퀀스 이름 
  initialValue = 1,
  allocationSize = 1)
//<<< DDD / Aggregate Root
public class Drive {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long driveId;

    private String driverName;

    private Long callId;

    private String driveStatus;

    private String taxiNum;

    private BigDecimal fare;
...

    @PostPersist
    @Transactional
    public void onPostPersist() {

        if("start".equals(this.getDriveStatus())) {
            DriveStarted driveStarted = new DriveStarted(this);
            driveStarted.publishAfterCommit();

        }else if("requestCancel".equals(this.getDriveStatus())) {
            DriveNotAvailavled driveNotAvailavled = new DriveNotAvailavled(this);
            driveNotAvailavled.publishAfterCommit();

        }

    }

...
    @Transactional
    public static void requestDriver(FarePaid farePaid) {

        Drive drive = new Drive();
        drive.setCallId(farePaid.getCallId());
        drive.setFare(farePaid.getFare());
        String taxiNum = String.valueOf(farePaid.getCallId() * 1111).substring(0, 4);
        drive.setTaxiNum(taxiNum);
        drive.setDriverName("driver"+taxiNum);

        BigDecimal a = new BigDecimal(1000000);

        if((farePaid.getFare().compareTo(a)) != 1) {
            drive.setDriveStatus("start");
        }else {
            drive.setDriveStatus("requestCancel");;
        }

        repository().save(drive);
    }

}


```
- 택시 call 시스템의 이벤트 드리븐한 Flow
```
1. user가 택시 call 선택 시 payment 서비스의 결제 로직이 수행되고, 결제가 완료되면 'farePaid' 이벤트를 Pub 한다
2. drive 모듈에서 'farePaid' 이벤트 수신 시 드라이브 데이터 변경 & 'driveStarted' 이벤트를 Pub 한다.
3. driver가 운행종료 수행 시  'driveEnded' 이벤트가 Pub 된다.
4. call 모듈에서 'driveEnded' 이벤트를 Sub 할 경우 callStatus를 'driveComplete'로 바꾼다
```
   - http 명령어를 사용하여 사용자ID, 사용자명, 거리 데이터를 넘겨 call 1건을 등록한다
   - ![image](https://github.com/dseojin/taxisvc/assets/173647509/b0b1b0e6-1cd4-414a-978a-b69c4c61ce1f)

   - kafka client 확인 시 요금지불, 콜요청, 드라이브시작 이벤트 발행이 확인된다
   - ![image](https://github.com/dseojin/taxisvc/assets/173647509/49bd3689-de3c-44db-a12f-c218e7ecb29f)

   - /drives/end url에 드라이브ID를 전달하여 해당 드라이브ID를 운행종료시킨다.
   - ![image](https://github.com/dseojin/taxisvc/assets/173647509/5a5045d5-da61-4fad-bee2-e110cdb69185)

   - kafka client 확인 시 drive 종료 이벤트 발행이 확인된다.
   - ![image](https://github.com/dseojin/taxisvc/assets/173647509/59076647-aadb-4783-905b-1df9f73fb986)

   - 운행종료 이후 call 상태 확인 시 'driveComplete' 로 변경이 확인된다.
   - ![image](https://github.com/dseojin/taxisvc/assets/173647509/d13b223e-d0a0-4542-9037-cb75312b4b42)


### 3.2 보상처리
- 비즈니스 예외 케이스로 운행불가 시 call의 상태 변경을 통해 데이터를 동기화한다.
```
1. 거리 기준 초과로 드라이버 배정 불가 시 'driveNotAvaliabled' 이벤트를 Pub 한다
2. call 모듈에서 'driveNotAvaliabled' 이벤트 수신 시 call 상태를 requestCancel로 변경 후 'callCancelled' 이벤트를 발행한다.
3. payment 모듈에서 'callCancelled' 이벤트 Sub 시 pay cancel 로직을 수행한다.
```
 - call 1건 등록 완료.(callID = 2)
 - ![image](https://github.com/dseojin/taxisvc/assets/173647509/657da825-40ce-4a86-9ea7-45a31b6c4456)

 - kafka client 확인 시 callId 2번에 드라이버 배정 불가 이벤트가 발행됨을 확인한다.
 - ![image](https://github.com/dseojin/taxisvc/assets/173647509/ca7f80ed-8d58-4c06-9b57-1015815de978)

 - 드라이버 배정 불가함에 따라 call 상태가 요청취소로 변경됨을 확인한다.
 - ![image](https://github.com/dseojin/taxisvc/assets/173647509/00e2fb60-3c24-484b-9ebd-3e5db5e5e795)

 - callCancelled 이벤트를 수신한 payment 서비스는 결제 취소 로직을 수행한다. (로직은 log로 대체)
 - ![image](https://github.com/dseojin/taxisvc/assets/173647509/9dd7135b-1783-428c-8946-45275cbd97f1)



### 3.3 단일진입점 : Gateway 서비스를 구현
```
// gqteway 서비스의 application.ymal 파일 내에 라우팅 설정을 통해 gateway 포트로 진입 시 다른 서비스로 진입하도록 구현하였다.
// gateway/src/main/resources/application.yml
...
spring:
  profiles: default
  cloud:
    gateway:
#<<< API Gateway / Routes
      routes:
        - id: call
          uri: http://localhost:8082
          predicates:
            - Path=/calls/**, 
        - id: payment
          uri: http://localhost:8083
          predicates:
            - Path=/payments/**, 
        - id: drive
          uri: http://localhost:8084
          predicates:
            - Path=/drives/**, 
        - id: mypage
          uri: http://localhost:8085
          predicates:
            - Path=/callViews/**, 
        - id: frontend
          uri: http://localhost:8080
          predicates:
            - Path=/**
...
```
- gateway port로 call 서비스 호출 (port 8088)
- ![image](https://github.com/dseojin/taxisvc/assets/173647509/8517ae7b-1c0a-4a6a-9db0-cd608a5e809c)

- gateway port로 drive 서비스 호출 (port 8088)
- ![image](https://github.com/dseojin/taxisvc/assets/173647509/ab1d6097-87d2-4488-a76d-4f83a8528983)


### 3.4 분산 데이터 프로젝션 (CQRS)
```
// mypage 서비스에서는 타서비스들에서 발행된 이벤트들을 읽어 데이터를 저장하는 CallViewViewHandler 를 생성하여 데이터 프로젝션을 수행하였다.
// mypage/src/main/java/taxisvc/infra/CallViewViewHandler.java

...
@Service
public class CallViewViewHandler {

    //<<< DDD / CQRS
    @Autowired
    private CallViewRepository callViewRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void whenCallPlaced_then_CREATE_1(@Payload CallPlaced callPlaced) {
        try {
            if (!callPlaced.validate()) return;

            // view 객체 생성
            CallView callView = new CallView();
            // view 객체에 이벤트의 Value 를 set 함
            callView.setCallId(callPlaced.getCallId());
            callView.setCallStatus(callPlaced.getCallStatus());
            callView.setUserName(callPlaced.getUserName());
            // view 레파지 토리에 save
            callViewRepository.save(callView);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @StreamListener(KafkaProcessor.INPUT)
    public void whenCallCancelled_then_UPDATE_1(
        @Payload CallCancelled callCancelled
    ) {
        try {
            if (!callCancelled.validate()) return;
            // view 객체 조회

            List<CallView> callViewList = callViewRepository.findByCallId(
                callCancelled.getCallId()
            );
            for (CallView callView : callViewList) {
                // view 객체에 이벤트의 eventDirectValue 를 set 함
                callView.setCallStatus(callCancelled.getCallStatus());
                // view 레파지 토리에 save
                callViewRepository.save(callView);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
...
```
- call 1건을 등록한 후, CallView 의 내용을 확인한다
- ![image](https://github.com/dseojin/taxisvc/assets/173647509/c197c087-f43a-4504-b593-345ba76192fe)
- ![image](https://github.com/dseojin/taxisvc/assets/173647509/24f35b38-b755-4e90-b96f-3604cdcebfd4)

- drive 서비스(8084)를 다운시킨 다음, CallView 의 내용을 확인하여도 서비스가 안정적임을 확인한다.
- ![image](https://github.com/dseojin/taxisvc/assets/173647509/a5fafba3-eb12-4bc6-8a56-73b30787e0d8)
- ![image](https://github.com/dseojin/taxisvc/assets/173647509/8e9cf889-c527-43be-8801-fae50ab467f3)

## 4. 운영
### 4.1 클라우드 배포 - Container 운영

### 4.2 컨테이너 자동확장 - HPA
- call 서비스에 Auto Scale-Out 설정 후 설정값 확인
- ![image](https://github.com/dseojin/taxisvc/assets/173647509/bc4d6fc6-b948-4080-9b1f-721c15537ed5)

- seige 명령으로 부하를 주어서 Pod 가 늘어나도록 한다
- siege -c20 -t40S -v http://call:8080/calls
- ![image](https://github.com/dseojin/taxisvc/assets/173647509/ceeb66b5-ed9f-4196-88fa-bd4d6899ef22)
  
- kubectl get po -w 명령을 사용하여 pod 가 생성되는 것을 확인한다.
- ![image](https://github.com/dseojin/taxisvc/assets/173647509/9127ab81-7c76-493d-829e-fade5181bf02)

- kubectl get hpa 명령어로 CPU 값이 늘어난 것을 확인 한다.
- ![image](https://github.com/dseojin/taxisvc/assets/173647509/2aee2c2a-ac3e-43aa-876c-45d9000ada28)

### 4.3 컨테이너 환경분리 - configMap
- call 서비스에 configMap 을 사용하기 위해 deployment.yaml, application.yml 수정
- configMap으로 log level을 'DEBUG'로 설정
  ```
  /workspace/taxisvc/call/kubernetes/deployment.yaml
  apiVersion: apps/v1
  kind: Deployment
  metadata:
    name: call
  ...
          env:
          - name: ORDER_LOG_LEVEL
            valueFrom:
              configMapKeyRef:
                name: config-dev
                key: ORDER_LOG_LEVEL
  ...


  /workspace/taxisvc/call/src/main/resources/application.yml
  spring:
  profiles: docker
  ...

  logging:
    level:
      root: ${ORDER_LOG_LEVEL}
      org:
        hibernate:
          SQL: ${ORDER_LOG_LEVEL}
        springframework:
          cloud: ${ORDER_LOG_LEVEL}
  ```
- YAML 기반의 ConfigMap을 생성
- ![image](https://github.com/dseojin/taxisvc/assets/173647509/744eaf36-fce8-4829-aa2c-57123e15a656)

- call 서비스 재배포
- ![image](https://github.com/dseojin/taxisvc/assets/173647509/646958c3-d7a1-42b7-a0b6-04090b0e97f2)

- log level 이 'DEBUG'로 적용됨을 확인한다.
- ![image](https://github.com/dseojin/taxisvc/assets/173647509/dc15342a-62dc-41ff-92d5-be46c0774726)

- configMap으로 log level을 'INFO'로 변경
- ![image](https://github.com/dseojin/taxisvc/assets/173647509/de521d24-2296-4c1e-a3f0-03b163574638)

- call 서비스 재배포 후 log level 이 'INFO'로 변경됨을 확인한다.
- ![image](https://github.com/dseojin/taxisvc/assets/173647509/e19a2421-1207-4823-a05d-38ddcd4a394e)

### 4.4 클라우드스토리지 활용 - PVC
- EBS CSI Driver 기반 gp3 StorageClass 등록
- ![image](https://github.com/dseojin/taxisvc/assets/173647509/95d99900-403b-497b-b0a3-bd0910cce011)

- Storage Class 확인
- ![image](https://github.com/dseojin/taxisvc/assets/173647509/a2e121fb-ba5b-4228-abd6-3aec78820587)

- PVC 생성
  ```
  kubectl apply -f - <<EOF
  apiVersion: v1
  kind: PersistentVolumeClaim
  metadata:
    name: aws-ebs
    labels:
      app: test-pvc
  spec:
    accessModes:
    - ReadWriteMany
    resources:
      requests:
        storage: 1Mi
    storageClassName: ebs-sc
  EOF
  ```
- pvc


### 4.5 무정지배포

### 4.6 서비스 메쉬 - istio
- Istio 설치 진행 후 자동으로 사이드카(Sidecar)를 Pod 내에 인잭션하도록 설정
```
kubectl label namespace default istio-injection=enabled
```
- pod 배포 및 pod 확인
- ![image](https://github.com/dseojin/taxisvc/assets/173647509/f7f30582-24fc-4039-9658-459fb7aab2d6)
- ![image](https://github.com/dseojin/taxisvc/assets/173647509/7135c520-b492-4709-a06f-71a92fb311b2)



### 4.7 통합모니터링 - grafana
- istio svc 조회
- ![image](https://github.com/dseojin/taxisvc/assets/173647509/6627ce81-b545-477c-94c1-aa818c94ffde)

- Grafana 서비스 Open - Service Scope을 LoadBalancer Type으로 수정
- ![image](https://github.com/dseojin/taxisvc/assets/173647509/1dfe09da-3e2a-483e-a66e-2fb76c196fd0)


- Grafana External IP 접속 후 Istio Service Dashboard 조회
- ![image](https://github.com/dseojin/taxisvc/assets/173647509/d9030c4c-5f1d-4588-b374-814673ece042)


- Grafana providing Dashboard 활용하기
- Siege 터미널에서 call 서비스로 부하를 발생 
- ![image](https://github.com/dseojin/taxisvc/assets/173647509/84d4cfb8-34e4-45bc-83d0-5c77b01a8303)

- 부하량에 따른 서비스 차트의 실시간 Gauge 확인
- ![image](https://github.com/dseojin/taxisvc/assets/173647509/b2c9e392-b96e-4409-b2f3-835bc247bfd9)






```
gitpod.io/#/github.com/dseojin/taxisvc

//로컬 카프카
cd infra
docker-compose exec -it kafka /bin/bash
cd /bin
./kafka-console-consumer --bootstrap-server http://localhost:9092 --topic taxisvc --from-beginning

//클러스터에 Event Store(kafka) 설치
curl https://raw.githubusercontent.com/helm/helm/master/scripts/get-helm-3 > get_helm.sh
chmod 700 get_helm.sh
./get_helm.sh
helm repo add bitnami https://charts.bitnami.com/bitnami
helm repo update
helm install my-kafka bitnami/kafka --version 23.0.5

    PRODUCER:
        kafka-console-producer.sh \
            --broker-list my-kafka-0.my-kafka-headless.default.svc.cluster.local:9092 \
            --topic test

    CONSUMER:
        kafka-console-consumer.sh \
            --bootstrap-server my-kafka.default.svc.cluster.local:9092 \
            --topic test \
            --from-beginning

my-kafka-0.my-kafka-headless.default.svc.cluster.local:9092

cd infra
docker-compose exec -it kafka /bin/bash
cd /bin

kubectl exec --tty -i my-kafka-client --namespace default -- bash
// 토픽 생성
./kafka-topics --bootstrap-server http://localhost:9092 --topic taxisvc --create --partitions 1 --replication-factor 1
 kafka-console-consumer.sh --bootstrap-server my-kafka.default.svc.cluster.local:9092 --topic taxisvc --create --partitions 1 --replication-factor 1

// 토픽에 메시지 삭제
./kafka-topics --delete --bootstrap-server http://localhost:9092 --topic taxisvc 

// 토픽 리스트조회
./kafka-topics --bootstrap-server http://localhost:9092 --list --exclude-internal  

// 토픽 consumer
./kafka-console-consumer --bootstrap-server http://localhost:9092 --topic taxisvc --from-beginning

mvn spring-boot:run


http get :8088/calls
http get :8088/calls/1
http :8088/calls userId=1 userName=eojin distance=10
http :8088/calls/cancel callId=1
http :8088/drives/end driveId=1
http :8085/callViews

```

# 

## Model
www.msaez.io/#/173647509/storming/taxisvc

## Before Running Services
### Make sure there is a Kafka server running
```
cd kafka
docker-compose up
```
- Check the Kafka messages:
```
cd infra
docker-compose exec -it kafka /bin/bash
cd /bin
./kafka-console-consumer --bootstrap-server localhost:9092 --topic
```

## Run the backend micro-services
See the README.md files inside the each microservices directory:

- call
- payment
- drive
- mypage


## Run API Gateway (Spring Gateway)
```
cd gateway
mvn spring-boot:run
```

## Test by API
- call
```
 http :8088/calls callId="callId" userId="userId" userName="userName" callStatus="callStatus" distance="distance" 
```
- payment
```
 http :8088/payments payId="payId" callId="callId" fare="fare" 
```
- drive
```
 http :8088/drives driveId="driveId" driverName="driverName" callId="callId" driveStatus="driveStatus" taxiNum="taxiNum" 
```
- mypage
```
```


## Run the frontend
```
cd frontend
npm i
npm run serve
```

## Test by UI
Open a browser to localhost:8088

## Required Utilities

- httpie (alternative for curl / POSTMAN) and network utils
```
sudo apt-get update
sudo apt-get install net-tools
sudo apt install iputils-ping
pip install httpie
```

- kubernetes utilities (kubectl)
```
curl -LO "https://dl.k8s.io/release/$(curl -L -s https://dl.k8s.io/release/stable.txt)/bin/linux/amd64/kubectl"
sudo install -o root -g root -m 0755 kubectl /usr/local/bin/kubectl
```

- aws cli (aws)
```
curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o "awscliv2.zip"
unzip awscliv2.zip
sudo ./aws/install
```

- eksctl 
```
curl --silent --location "https://github.com/weaveworks/eksctl/releases/latest/download/eksctl_$(uname -s)_amd64.tar.gz" | tar xz -C /tmp
sudo mv /tmp/eksctl /usr/local/bin
```

