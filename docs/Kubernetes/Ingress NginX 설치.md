# Ingress NginX 설치

> https://kubernetes.github.io/ingress-nginx/deploy/#quick-start

Yaml mnifest 기반 설치

```bash
$ kubectl apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/controller-v1.13.0/deploy/static/provider/cloud/deploy.yaml
```

### Pre-flight Check

- [ingress-nginx 버전 테이블 확인](https://github.com/kubernetes/ingress-nginx?tab=readme-ov-file#supported-versions-table)
- Port 80, 443 이 기본으로 사용됨
- Port 8443 은 ingress-nginx admission controller 으로 사용됨
  - 리소스 수정 시, 해당 내용을 검증하는 API

### Ingress, Ingress Controller

- Ingress는 단순히 규칙을 선언하는 오브젝트이다.
- Ingress Controller에서 실제 외부 요청을 받아 처리하며, Ingress가 Controller에 적용되었을 때에 규칙이 활성화 된다.
- 이 때, Ingress Controller 중 하나가 Ingress NginX이다.
