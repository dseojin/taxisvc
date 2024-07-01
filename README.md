# CALL TAXI SERVICE

## 1. 아키텍처
### 1.1 MSA 아키텍처 구성도


## 2. 모델링
### 2.1 이벤트 스토밍
![image](https://github.com/dseojin/taxisvc/assets/173647509/07756d1f-759e-4a14-a73b-95d849029869)

## 3. 구현
### 3.1 분산트랜잭션

### 3.2 보상처리
#### 거리가 너무 멀어 드라이버 배정 불가 시 call status가 'cancel'로 변경됨

### 3.3 단일진입점 : Gateway 서비스를 구현
#### call 서비스 호출 (port 8082)

#### gateway port로 call 서비스 호출 (port 8088)

### 3.4 분산 데이터 프로젝션 (CQRS)
#### callView 서비스를 호출하여 call, drive 데이터 확인

#### drive 서비스를 down 후

#### callView 서비스를 호출 시 정상으로 call, drive 데이터 확인 가능





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

