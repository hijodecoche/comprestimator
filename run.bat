:: batch file to call comprestimator
@ECHO off

IF NOT EXIST "skip_list.txt" (
    @ECHO /proc > "%cd%\skip_list.txt"
    @ECHO /sys >> "%cd%\skip_list.txt"
    @ECHO /dev >> "%cd%\skip_list.txt"
    @ECHO /snap >> "%cd%\skip_list.txt"
    @ECHO /run >> "%cd%\skip_list.txt"
)

Start java -Xmx6g -jar comprestimator.jar
