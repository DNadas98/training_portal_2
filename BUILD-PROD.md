### Build and push to DockerHub
```Bash
docker login
```
```Bash
cd backend && \
docker build -f Dockerfile.prod -t dnadas98/priv:training_portal_backend2 . && \
docker push dnadas98/priv:training_portal_backend2 && \
cd ..
```
```Bash
cd frontend && \
docker build -f Dockerfile.prod -t dnadas98/priv:training_portal_frontend2 . && \
docker push dnadas98/priv:training_portal_frontend2 && \
cd ..
```