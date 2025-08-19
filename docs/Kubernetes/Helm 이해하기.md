# Helm

- 컨테이너화된 애플리케이션의 배포와 관리를 돕는 패키지 관리자.
- Helm을 이용하여 복잡한 애플리케이션을 정의하고 설치 및 관리할 수 있음
- Helm Chart는 kubernetes 리소스와 응용 프로그램간의 종속성, 템플릿 및 다양한 메타정보를 관리하기 위한 패키지 정보를 담고 있는 파일
- 조직 전체에서 공유 및 사용 할 수 있기 때문에 stage, producation, development 등 다양한 환경에서 쉽게 배포 가능
- 또한 버전관리, 배포방식, 롤백 등의 기능도 가지고 있어, 릴리즈 쉽게 관리 가능

## 기본 명령어

> https://helm.sh/ko/docs/intro/using_helm/

```bash
# Helm 저장소 추가
$ helm repo add bitnami https://charts.bitnami.com/bitnami

# Helm 저장소 조회
$ helm repo list

# Helm chart 설치 (Release 추가)
$ helm install [RELEASE NAME] [CHART NAME]
$ helm install phpmyadmin bitnami/phpmyadmin
$ helm install phpmyadmin bitnami/phpmyadmin -f ./values.yaml # values 파일로 설정값 덮어쓰기
$ helm install foo path/to/foo # 압축 해제된 차트 디렉토리

# Release 목록 확인
$ helm list

# Release 삭제
$ helm uninstall [RELEASE NAME]
```
