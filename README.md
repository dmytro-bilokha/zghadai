# Zghadai

Zghadai is a simple photo and video gallery webapp developed for home use. 

## Requirements

1. The webapp should be deployed in **TomEE Plus 10.1** or later. It should also work within any other application server
   which provides full set of Jakarta EE 10 APIs implementation, but may require small tweaks.
2. The thumbnails generating script uses **ImageMagick** to process images and **FFmpeg** for video files, corresponding
   binary files should be installed and be available in `$PATH`.

## Deployment and configuration

### Configuring TomEE
Before deploying the webapp TomEE should be configured:
1. Add `tomee.mp.scan = all` to `conf/system.properties` file.
2. Ensure that on TomEE start JVM sets property value for the `zghadai.content.root` to the directory which contains
   gallery files, e.g. something like `java -Dzghadai.content.root=/media/my_family ...`.
3. Ensure TomEE user has access to the gallery directories and files (can browse directories and read files).

### Generating thumbnails

In order for gallery to display nice thumbnails, it requires them to be pre-generated with a script `src/script/generate-thumbnails`.
The script accepts only one argument: path to the directory with media files. It will go through the given directory and
all its subdirectories, create `thumbnails` subdirectories and fill them with thumbnails.
Example of the command to execute: `generate-thumbnails /media/my_family`.
   
### Deploying the webapp
Build and deploy the `zghadai.war` artifact into TomEE in a preferred way (using  `manager` webapp, dropping war in
`webapps` directory, etc.).