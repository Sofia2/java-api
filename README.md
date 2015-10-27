# Sofia2 Java API

*Read this in other languages: [English](README.md), [Spanish](README.es.md).*

## Copyright notice

© 2013-15 Indra Sistemas S.A.

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0.

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.

## API documentation

Before using the SSAP API for the first time, we strongly recommend that you learn the main concepts of the Sofia2 platform. They have been included in the Sofia2 developer documentation, which can be downloaded from http://sofia2.com/desarrollador_en.html.

## Build instructions

The Java API is distributed as a maven project. Its artifact can be built and installed in the local maven repository using the following commands:

```
cd <root of your copy of the repository>
mvn clean package [-Dmaven.test.skip]
mvn install:install-file -DpomFile=pom.xml -Dfile=target/ssap-standalone-<API version>.jar
```

During the compilation process, a "fat" JAR containing the API and its dependencies will also be generated. It will be placed in the path

```
<root of your copy of the repository>/target/ssap-standalone-with-deps-<API-version>.jar
```

## Información de contacto

Si necesita recibir soporte, puede contactar con nosotros en sofia2.com o enviando un correo electrónico a [plataformasofia2@indra.es](mailto:plataformasofia2@indra.es).

Además, si desea contribuir al API C, no dude en enviarnos una pull request.