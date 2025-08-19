# Kubernetes 주요 개념

- Pod: 실질적인 프로세스 이자 컨테이너
  - 프로세스 이름, 프로세스를 실행할 이미지 이름, 사용할 포트 등의 명세서 필요
- Deployment: Pod과 Node 상태를 모니터링 하다가 장애가 발생할 경우, Pod을 새로 실행하거나 교체하는 작업을 자동으로 수행
  - Pod들이 정상적으로 서비스를 할 수 있도록 도움
- Service: Pod들의 논리적인 묶음 형태로 느슨한 결합을 지원, pod들에게 접근할 수 있는 정책을 정의 (internal IP를 통해 트래픽 유입)
  - 실질적으로 Pod들에게 트래픽이 들어가도록 브릿지 역할 (네트워크 구조 생성)

# Deployment

```yaml
# Deployment 샘플
apiVersion: apps/v1
kind: Deployment
metadata:
  name: nginx-deployment
  labels:
    app: nginx
spec:
  replicas: 3
  selector:
    matchLabels:
      app: nginx
  template:
    metadata:
      labels:
        app: nginx
    spec:
      containers:
        - name: nginx
          image: nginx:1.14.2
          ports:
            - containerPort: 80
```

```bash
# Deployment 설정파일을 통한 실행
$ kubectl apply -f ./nginx.yaml

# Deployment 상태 확인 (deployment -> deploy 축약 가능)
$ kubectl get deployments
NAME               READY   UP-TO-DATE   AVAILABLE   AGE
nginx-deployment   3/3     3            3           16s

# rollout: deployment의 상태를 확인
$ kubectl rollout status deployment/nginx-deployment
deployment "nginx-deployment" successfully rolled out

# get rs (replica set) 명령으로 리플리카셋의 정보
$ kubectl get rs
NAME                          DESIRED   CURRENT   READY   AGE
nginx-deployment-85996f8dbd   3         3         3       38m
```

이전 yaml 설정 파일을 수정하여 적용할 경우, 변경된 스펙이 적용된다.
만약 `spec.replicas` 3 -> 4 로 변경할 경우, 또는 이미지를 변경할 경우, 아래와 같이 적용할 수 있다.

```bash
$ kubectl apply -f ./nginx.yaml
deployment.apps/nginx-deployment configured

$ kubectl get deployments
NAME               READY   UP-TO-DATE   AVAILABLE   AGE
nginx-deployment   4/4     4            4           73m
```

### Rollout

이미지가 업데이트 되는 등 애플리케이션 변경이 발생할 경우 history로 변경이 남아, rollback을 진행할 수 있다

```bash
# 만약 버전을 변경한 경우
$ kubectl rollout history deploy nginx-deployment
deployment.apps/nginx-deployment
REVISION  CHANGE-CAUSE
1         <none>
2         <none>

# 각 revision 상세 정보를 보는 법
$ kubectl rollout history deploy nginx-deployment --revision=2
deployment.apps/nginx-deployment with revision #2
Pod Template:
  Labels:	app=nginx
	pod-template-hash=66f8758855
  Containers:
   nginx:
    Image:	nginx:1.16.1
    Port:	80/TCP
    Host Port:	0/TCP
    Environment:
    Mounts:
  Volumes:
```

롤백을 원하는 경우, undo를 통해 이전 버전으로 롤백되며, 실질적인 revision이 이전으로 돌아가는게 아닌, 3으로 설정됨을 확인할 수 있다.
다시 revision 2로 돌아가고 싶을 경우 아래와 같이 실행하면 된다.

```bash
$ kubectl rollout undo deploy nginx-deployment

$ kubectl rollout history deploy nginx-deployment
deployment.apps/nginx-deployment
REVISION  CHANGE-CAUSE
1         <none>
2         <none>
3         <none>

$ kubectl rollout history deploy nginx-deployment --to-revieision=2
```

### Deploy Strategy

- Recreate Deployment: 현재 실행 중인 pod 종료 및 새 버전 pod 동시 새로 실행 (중단 발생, 복구 지연)
- Rolling Update Deployment: 실행 중인 pod들을 순차적으로 새로운 버전으로 업데이트하는 전략
- Blue/Green Deployment: 실행 중인 pod들은 유지한 채, 신규 버전의 pod들을 생성해 트래픽 라우팅을 변경하는 방식으로 변경
- Canary Deploymnet: 실행 중인 Pod 중 일부분만 점차적으로 업데이트 하는 전략

---

# Service

이전에 설정 파일을 통해 생성한 Deployment를 외부에서 접근 가능하도록 expose 할 경우, 서비스가 생긴다.
expose 시에 클라우드 공급자(ex. AWS, GCP)를 사용할 경우 Public IP를 통해 연결 되지만, 로컬에는 public IP가 없기 때문에 port-forward를 통해 검증한다.

```bash
$ kubectl expose deployments hello-world --type=LoadBalancer --port=8080

# local의 경우
$ kubectl port-forward service/hello-world 7080:8080
```

### Service Type

- Cluster IP: 클러스너 태부에 새로운 IP 할당하고 여러 Pod들을 바라보는 Load Balancer 형태로 작동
- NodePort: 각 Node에 잇는 특정 port를 이용해 서비스를 노출. 쉽게 접근이 가능하여, 개발 또는 테스트 목적으로 사용
- LoadBalancer: 클라우드 공급자의 Load Balander를 사용하여 서비스를 외부에 노출한다.
- Ingress: 클러스터 외부에서 내부로 HTTP, HTTPS 경로를 제공하기 위해 사용 (L7 LoadBalander)
  - 복잡한 라우팅 가능
  - LoadBalander와 유사

### Service - NodePort

NodePort 타입의 서비스를 만들기 위해서는 아래와 같은 서비스 설정을 가진다.

```yaml
apiVersion: v1
kind: Service
metadata:
  name: hello-world-service
spec:
  selector:
    run: hello-world-app
  ports:
    - port: 5000
  type: NodePort
```

```bash
$ kubectl apply -f ./hello-service.yaml

$ kubectl get service hello-world-service
NAME                  TYPE       CLUSTER-IP      EXTERNAL-IP   PORT(S)          AGE
hello-world-service   NodePort   10.99.149.177   <none>        5000:30332/TCP   25m
```

#### Service - Ingress

클러스터 외부에서 내부에 있는 서비스로 HTTP, HTTPS 요청을 전달할 수 있도록 하는 L7 Load Balander Interface를 제공한다. (Ingress는 단순 interface이고, NginX, Traefik, Kong 등을 사용)

하는 일은 다음과 같다.

- URL, HOST 또는 기타 메타데이터를 기반으로 서비스들을 routing
- 트래픽에 대한 SSL 인증서를 처리
- 들어오는 트래픽에 대한 부하 분산
- 속도 제한 (Rate Limiting), 사용자 인증과 같은 다양한 기능 제공

```bash
# addon 명령어를 통해 nginx ingress 설치 가능
$ minikube addons enable ingress

$ kubectl get pods -n ingress-nginx
NAME                                       READY   STATUS      RESTARTS   AGE
ingress-nginx-admission-create-x992s       0/1     Completed   0          49s
ingress-nginx-admission-patch-5ctd9        0/1     Completed   0          49s
ingress-nginx-controller-67c5cb88f-9znf2   1/1     Running     0          49s
```

ingress 샘플 설정은 아래와 같다.

```yaml
# path 기반 라우팅
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: media-ingress
spec:
  rules:
    - http:
        paths:
          - path: /music
            pathType: Prefix
            backend:
              service:
                name: music-service
                port:
                  number: 8081
          - path: /photo
            pathType: Prefix
            backend:
              service:
                name: photo-service
                port:
                  number: 8080

---
# host 기반 라우팅
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: media-ingress
spec:
  rules:
    - host: "music.example.com"
      http:
        paths:
          - path: /
            pathType: Prefix
            backend:
              service:
                name: music-service
                port:
                  number: 8088
    - host: "photo.example.com"
      http:
        paths:
          - path: /
            pathType: Prefix
            backend:
              service:
                name: photo-service
                port:
                  number: 8080
```

```bash
# ingress 실행
$ kubectl apply -f media-ingress.yaml

$ kubectl get ingress
NAME            CLASS   HOSTS   ADDRESS        PORTS   AGE
media-ingress   nginx   *       192.168.49.2   80      18m

$ kubectl describe ingress media-ingress
Name:             media-ingress
Labels:           <none>
Namespace:        default
Address:          192.168.49.2
Ingress Class:    nginx
Default backend:  <default>
Rules:
  Host        Path  Backends
  ----        ----  --------
  *
              /music   music-service:8081 (10.244.0.53:8081,10.244.0.54:8081)
              /photo   photo-service:8080 (10.244.0.55:8080,10.244.0.56:8080)
Annotations:  <none>
Events:
  Type    Reason  Age                From                      Message
  ----    ------  ----               ----                      -------
  Normal  Sync    18m (x4 over 25m)  nginx-ingress-controller  Scheduled for sync
```
