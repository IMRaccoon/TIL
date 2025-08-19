## Requirements

- k8s version >= 1.10
- nvidia driver installed
- nvidia-container-toolkit >= 1.0.0
- nvidia-container-runtime set as default runtime
  - docker: /etc/docker/daemon.json 내 default-runtime 선언

```json
{
  "default-runtime": "nvidia",
  "runtimes": {
    "nvidia": {
      "path": "/usr/bin/nvidia-container-runtime",
      "runtimeArgs": []
    }
  }
}
```

```shell
docker systmectl restart docker
```

## Execute by Kubernetes

- yaml 매니페스트 (2025.07.25 기준): https://raw.githubusercontent.com/NVIDIA/k8s-device-plugin/v0.17.3/deployments/static/nvidia-device-plugin.yml

```shell
wget https://raw.githubusercontent.com/NVIDIA/k8s-device-plugin/v0.17.3/deployments/static/nvidia-device-plugin.yml
kubectl apply -f nvidia-device-plugin.yml
```

정상 적용되었는지 확인하기 위해서는 아래의 스크립트 참고

```
$ kubectl get daemonset -n kube-system

>>>
NAMESPACE      NAME                             DESIRED   CURRENT   READY   UP-TO-DATE   AVAILABLE   NODE SELECTOR            AGE
kube-system    kube-proxy                       5         5         5       5            5           kubernetes.io/os=linux   5m56s
kube-system    nvidia-device-plugin-daemonset   4         4         4       4            4           <none>                   38s
```

이후 적용하고자 하는 Container의 Resource에 gpu를 추가해주면 된다

```yaml
spec:
  nodeName:
  containers:
    - name:
      image:
      resources:
        limits:
          cpu:
          memory:
          nvidia.com/gpu: "1"
```

## Execute by Helm

- Begin by setting up the plugin's helm repository and updating it at follows

```bash
helm repo add nvdp https://nvidia.github.io/k8s-device-plugin
helm repo update
```

Then verify that the latest release (v0.17.3) of the plugin is available:

```bash
$ helm search repo nvdp --devel
NAME                     	  CHART VERSION  APP VERSION	DESCRIPTION
nvdp/nvidia-device-plugin	  0.17.3	 0.17.3		A Helm chart for ...
```

Once this repo is updated, you can begin installing packages from it to deploy the nvidia-device-plugin helm chart.

The most basic installation command without any options is then:

```bash
helm upgrade -i nvdp nvdp/nvidia-device-plugin \
  --namespace nvidia-device-plugin \
  --create-namespace \
  --version 0.17.3
```

기타 Config 설정을 위해서는 링크 참고: https://github.com/NVIDIA/k8s-device-plugin?tab=readme-ov-file#configuring-the-device-plugins-helm-chart
