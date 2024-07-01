# CALL TAXI SERVICE

## 1. 아키텍처
### 1.1 MSA 아키텍처 구성도
![image](https://github.com/dseojin/taxisvc/assets/173647509/4e685e32-3e15-456a-9a30-f92b8b7ce571)


## 2. 모델링
### 2.1 이벤트 스토밍
![image](https://github.com/dseojin/taxisvc/assets/173647509/23bc009d-8397-4411-9ced-4703c4662eb7)


## 3. 구현
```
call : 8082
payment : 8083
drive : 8084
callView(CQRS) : 8085
gateway : 8088
```
### 3.1 분산트랜잭션
- 택시 call 이벤트 드리븐한 플로우
1. user가 택시 call 선택 시 'callPlaced' 이벤트가 Pub 된다.
2. payment 모듈에서 'callPlaced' 이벤트 수신 시 결제 로직이 수행되고, 결제가 완료되면 'farePaid' 이벤트를 Pub 한다
3. drive 모듈에서 'farePaid' 이벤트 수신 시 'driveStarted' 이벤트를 Pub 한다.
   - call http 수행
   - ![image](https://github.com/dseojin/taxisvc/assets/173647509/2028714f-a841-487a-9bfc-c200db727e97)

   - kafka - 콜요청, 요금지불, 드라이브시작 이벤트 발행 확인됨
   - ![image](https://github.com/dseojin/taxisvc/assets/173647509/73771fba-cbf6-4b79-822c-6a7e31bd3453)

- 운행종료 로직의 이벤트 드리븐한 플로우
1. 운행이 종료되어 driver가 운행종료 선택 시 'driveEnded' 이벤트가 Pub 된다.
2. call 모듈에서 'driveEnded' 이벤트를 Sub 할 경우 callStatus를 'driveComplete'로 바꾼다
   - end http - drive 종료 수행
   - ![image](https://github.com/dseojin/taxisvc/assets/173647509/ff3e31cc-5928-4fd7-bc8c-79bb05985819)

   - kafka - drive 종료 이벤트 발행됨
   - ![image](https://github.com/dseojin/taxisvc/assets/173647509/1d9d9e84-bbbd-4cc9-ad24-a9f538f50257)

   - call http - call 상태 확인 시 driveComplete 로 변경됨
   - ![image](https://github.com/dseojin/taxisvc/assets/173647509/232d19c1-7023-4539-9e52-3641b47ec47e)

 

### 3.2 보상처리
- 운행불가
1. 거리 기준 초과로 드라이버 배정 불가 시 'driveNotAvaliabled' 이벤트를 Pub 한다
2. call 모듈에서 'driveNotAvaliabled' 이벤트 수신 시 call 상태를 requestCancel로 변경 후 pay cancel 로직을 수행한다.
 - call 1건 등록 - callID = 3 :::
 - ![image](https://github.com/dseojin/taxisvc/assets/173647509/fdcaba92-bf4d-487d-82de-b3ee9737eab9)

 - kafka - callId 3번 드라이버 배정 불가 이벤트 :::
 - ![image](https://github.com/dseojin/taxisvc/assets/173647509/44c4bb7b-8d3f-4222-900e-866acd271957)

 - call status - call 의 상태가 요청취소로 변경됨:::
 - ![image](https://github.com/dseojin/taxisvc/assets/173647509/9a407dd0-9d1d-4018-af45-db16d16e2a75)

### 3.3 단일진입점 : Gateway 서비스를 구현
- gateway port로 call 서비스 호출 (port 8088)
- ![image](https://github.com/dseojin/taxisvc/assets/173647509/8517ae7b-1c0a-4a6a-9db0-cd608a5e809c)

- gateway port로 drive 서비스 호출 (port 8088)
- ![image](https://github.com/dseojin/taxisvc/assets/173647509/ab1d6097-87d2-4488-a76d-4f83a8528983)


### 3.4 분산 데이터 프로젝션 (CQRS)
- call 1건을 등록한 후, CallView 의 내용을 확인한다
- ![image](https://github.com/dseojin/taxisvc/assets/173647509/e0d4eac9-bccf-4149-a138-5c6b2e972ac6)

- drive 서비스(8084)를 다운시킨 다음, CallView 의 내용을 확인하여도 서비스가 안정적임을 확인한다.
- ![image](https://github.com/dseojin/taxisvc/assets/173647509/f13f1409-3ce3-43d5-81ed-002259665ade)


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

