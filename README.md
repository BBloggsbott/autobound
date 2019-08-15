# autobound
Repository for GSoC 2019 [project](https://summerofcode.withgoogle.com/projects/#5892666334642176).<br>

Currently Implemented Features:
* `Collect Data - AutoBound` - This can be found under the Tools Menu. This is used to download and save data to train the AutoBound's Image Segmentation Model.
* `AutoBound` MapMode - The plugin side of the tool is completed but the server side is still under development.

The above functionalities require the [AutoBound Server](https://github.com/BBloggsbott/autoboundserver/) to be running.<br>
Check the [wiki](https://github.com/BBloggsbott/autobound/wiki) for more info.

## Using the plugin
To launch JOSM with AutoBound, clone this repository and run the following command:<br>
If you have gradle installed, run<br>
```bash
$ gradle runJosm
```

If you don't have gradle installed, run either of the following commands<br>
If you are using Windows,
```bash
$ gradlew.bat runJosm
```
If you are using Linux/Mac
```bash
./gradlew runJosm
```