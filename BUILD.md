# Builder image
```
cd src/packages/docker-compile
docker build --build-arg uid=$(id -u) --build-arg gid=$(id -g) -t x-road-builder:latest .
```

## Run manual build

Execute from repository root
```
docker run -it --name builder -v $(pwd):/home/builder/repo -w /home/builder/repo x-road-builder:latest bash
```

Tests might fail
```
export GRADLE_OPTS='-Dorg.gradle.daemon=false'
export JAVA_HOME='/usr/lib/jvm/java-8-openjdk-amd64/'
cd src
./update_ruby_dependencies.sh
~/.rvm/bin/rvm jruby-$(cat .jruby-version) do ./gradlew --stacktrace --no-daemon buildAll runProxyTest runMetaserviceTest runProxymonitorMetaserviceTest jacocoTestReport dependencyCheckAggregate -Pstrict-frontend-audit -Pfrontend-unit-tests
```

# Packager image for bionic
```
cd src/packages/docker/deb-bionic
docker build -t x-road-packager-bionic:latest .
```

## Run manual packing

Execute from repository root
```
docker run -it --name packager-bionic -v $(pwd):/home/builder/repo -w /home/builder/repo x-road-packager-bionic:latest bash
```
