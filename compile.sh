# compile.sh

javac -d out comfaas/*.java
jar cmf manifest.txt ComFaaS.jar -C out/ .
