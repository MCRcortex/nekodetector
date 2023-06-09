# Neko Detector 

> A tool to help detect if you are infected by the fractureiser malware.

The fractureiser malware once you run it, infects any jar it is able to find. This tool will help you detect if you are infected by the malware by scanning every jar file in your computer and checking if it shows sign of infection. *For more information about the malware, please refer to the [information document](https://github.com/fractureiser-investigation/fractureiser/blob/main/README.md).*

## Usage


Most of the time you will only need to double click the jar file to run it, and it will launch with a gui. However, if you want to run it in the terminal, and still have the gui, you can run it with the following command:

```bash
# Replace jarscanner-VERSION-HERE.jar with the name of the jar file you downloaded from our releases page.
# The file name will be something like jarscanner-1.0.0.jar or jarscanner-1.1-SNAPSHOT.jar
java -jar jarscanner-VERSION-HERE.jar
```

## Advanced Usage

Usage Docs:
```
java -jar jarscanner-1.1-SNAPSHOT.jar <# of threads> <path to scan> <optional: 'true' for failed jar file opening errors>
```

Examples:
```bash
# Scan your entire Windows system with 4 threads
java -jar jarscanner-1.1-SNAPSHOT.jar 4 C:\

# Scan your entire Linux system with 4 threads
java -jar jarscanner-1.1-SNAPSHOT.jar 4 /

# Scan you entire Windows system with 4 threads, and print out errors when a jar file fails to open
java -jar jarscanner-1.1-SNAPSHOT.jar 4 C:\ true
```
