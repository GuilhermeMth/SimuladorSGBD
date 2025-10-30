# SimuladorSGBD

Este repositório contém o projeto "SimuladorSGBD", escrito em Java puro (sem usar frameworks/container como Maven/Gradle). Para executar o projeto localmente você precisará da biblioteca OpenJFX (JavaFX) porque o projeto usa JavaFX para a interface gráfica.

Abaixo está um guia passo a passo para baixar, instalar e configurar o OpenJFX e executar o projeto em diferentes ambientes (IDE e linha de comando).

---

## Requisitos

- Java JDK compatível (recomenda-se JDK 11, 17 ou superior).  
  - Importante: escolha uma versão do OpenJFX que corresponde à versão do JDK que você está usando.
- OpenJFX (JavaFX) — projeto openjfx (https://openjfx.io/).

---

## Baixando o OpenJFX

1. Acesse https://openjfx.io/  
2. Clique em "Download" -> você será direcionado para os binários correspondente à versão desejada no Gluon.  
3. Baixe o SDK apropriado para o seu sistema operacional (Windows, macOS, Linux) e para a versão do Java que você utiliza (por exemplo, 17 ou 11).  
4. Extraia o ZIP em um local de sua preferência. Após extrair, haverá uma pasta `lib` contendo os jars do JavaFX (por exemplo, `javafx-controls.jar`, `javafx-fxml.jar`, etc.).

Exemplo de caminho extraído:
- Windows: C:\javafx-sdk-20\lib
- macOS/Linux: /home/usuario/javafx-sdk-20/lib ou /Users/usuario/javafx-sdk-20/lib

---

## Executando no IntelliJ IDEA

1. Abra o projeto no IntelliJ.
2. Vá em Run -> Edit Configurations...
3. Selecione a configuração de execução do seu `Main` (ou crie uma nova Application).
4. Em "VM options" adicione (substitua o caminho pelo caminho para sua pasta `lib` do JavaFX):

   --module-path "C:\caminho\para\javafx-sdk-XX\lib" --add-modules=javafx.controls,javafx.fxml

   (No macOS/Linux):

   --module-path "/home/usuario/javafx-sdk-XX/lib" --add-modules=javafx.controls,javafx.fxml

5. Salve e execute.

Opcional (para compilação se necessário): configure nas Project Structure -> Libraries -> + -> Java -> aponte para os jars dentro da pasta `lib`.

---

## Executando no Eclipse

1. Importar o projeto como um projeto Java.
2. Vá em Run -> Run Configurations... -> Java Application.
3. Selecione a sua classe principal.
4. Na aba "Arguments" -> VM arguments, adicione:

   --module-path "/caminho/para/javafx-sdk-XX/lib" --add-modules=javafx.controls,javafx.fxml

5. Alternativamente, adicione todos os arquivos JAR do JavaFX como "External JARs" em Project -> Properties -> Java Build Path -> Libraries.

---

## Executando via linha de comando

Supondo que:
- O código fonte compilado esteja em `out/` (ou você compila direto dos .java).
- O diretório com o SDK do JavaFX está em `/caminho/para/javafx-sdk-XX/lib`.

Compilar (exemplo simples):
- Se o projeto não usa packages complexos e está em src/:
  javac --module-path "/caminho/para/javafx-sdk-XX/lib" -cp . -d out $(find src -name "*.java")

Executar:
  java --module-path "/caminho/para/javafx-sdk-XX/lib" --add-modules=javafx.controls,javafx.fxml -cp out com.seupacote.MainClass

Substitua `com.seupacote.MainClass` pela classe principal do projeto (classe que contém `public static void main(String[] args)` e chama `Application.launch(...)`).

Exemplo (Windows):
  javac --module-path "C:\javafx-sdk-20\lib" -cp . -d out src\com\exemplo\*.java
  java --module-path "C:\javafx-sdk-20\lib" --add-modules=javafx.controls,javafx.fxml -cp out com.exemplo.Main

Observação: em projetos sem módulos (non-modular), o parâmetro `--add-modules` é suficiente quando se usa `--module-path` apontando para os jars do JavaFX.

---

## Dicas e problemas comuns

- Erro "JavaFX runtime components are missing" ou similar: significa que o JavaFX não está no classpath/module-path. Verifique o `--module-path` e `--add-modules`.
- Versão incompatível: use uma versão do JavaFX que corresponde ao seu JDK (ex.: JDK 17 -> JavaFX 17.x ou compatível).
- Se você planeja empacotar a aplicação em um JAR executável, lembre-se de incluir os jars do JavaFX ou usar um empacotador (jlink/jpackage) que inclua o runtime.
- Para facilitar, você pode migrar o projeto futuramente para Maven/Gradle e adicionar as dependências do JavaFX via os artefatos do Maven Central (ex.: org.openjfx:javafx-controls:17) — isso automatiza o download e configuração.

---

## Exemplo rápido de VM options (recapitulando)
- Windows:
  --module-path "C:\javafx-sdk-XX\lib" --add-modules=javafx.controls,javafx.fxml

- macOS / Linux:
  --module-path "/home/usuario/javafx-sdk-XX/lib" --add-modules=javafx.controls,javafx.fxml

---

Se quiser, posso:
- Escrever comandos exatos substituindo o nome da sua classe principal se você me disser qual é.
- Gerar um script de execução (run.sh / run.bat) para o projeto que já inclua o caminho para o JavaFX (você indicaria onde extraiu o SDK).
- Ajudar a converter o projeto para usar Maven/Gradle para gerenciar automaticamente o JavaFX.

Bom desenvolvimento!
