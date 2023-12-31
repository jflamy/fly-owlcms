# syntax=docker/dockerfile:1

FROM eclipse-temurin:17-jdk-jammy
WORKDIR /app
ENV FLYCTL_INSTALL /app/fly

# put required utilities in /app/fly/bins
RUN <<EOF
wget -O jq https://github.com/jqlang/jq/releases/download/jq-1.6/jq-linux64
chmod +x ./jq
mkdir /app/fly
curl -L https://fly.io/install.sh | sh
mv jq /app/fly/bin
EOF

COPY target/fly-manager.jar /app
EXPOSE 8080
ENTRYPOINT ["/opt/java/openjdk/bin/java", "-jar", "fly-manager.jar"]