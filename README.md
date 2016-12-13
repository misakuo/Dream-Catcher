# Dream-Catcher
Inspection the Android HTTP(S) traffic in Chrome Developer Tools

## Introduction
Dream Catcher is a HTTP proxy based traffic analysis tools, provides the ability to view http(s) traffic in chrome by adapting the [Chrome Remote Debug Protocol](https://chromedevtools.github.io/debugger-protocol-viewer/).

## Usage
- Install and launch the [Dream Catcher APP](https://github.com/misakuo/Dream-Catcher/releases/download/release-1.0/dream_catcher-1.0.apk)
- Connect your device to PC, make sure the USB debugging is active and the `adb` is usable
- Click to enable the HTTP proxy (It's maybe need waiting a bit time)
- Click **Install Certificate** to install CA for enable [MITM](https://en.wikipedia.org/wiki/Man-in-the-middle_attack) (Dream Catcher will not do evil, it just be using to decryption the HTTPS traffic. If do not need HTTPS inspection, you can skip this step)
- Click **Trusted Credentials** to examine the MITM CA or remove it
- Click **Setting Proxy** to setup proxy on active connection (General steps: 1. long click the active connection; 2. select *Modify Network* on dialog; 3. select *Manual* on the *Proxy* options; 4. input `127.0.0.1` to the *Proxy hostname* textbox; 5. input proxy port (default is `9999`) to the *Proxy port* textbox; 6. click the *SAVE* button)
- Open Chrome and navigate to [chrome://inspect](chrome://inspect)
- Click **inspect** when Dream Catcher is ready
- Select the **Network** tab to start inspection

## Example
### The homepage
<image src="https://ooo.0o0.ooo/2016/12/13/584f7a044e1fa.png" width="40%"/>
### Enable proxy  
<image src="https://ooo.0o0.ooo/2016/12/13/584f7a04404aa.png" width="40%"/>
### Let's trying to visit Google Play
**Got it!**   
![QQ20161213-0.png](https://ooo.0o0.ooo/2016/12/13/584f7a42daf42.png)
**The headers**    
![QQ20161213-1.png](https://ooo.0o0.ooo/2016/12/13/584f7a42ac344.png)
### Preview response
**Image**    
![QQ20161213-2.png](https://ooo.0o0.ooo/2016/12/13/584f7a422daf8.png)
**JSON**
![QQ20161213-3.png](https://ooo.0o0.ooo/2016/12/13/584f7a4230aba.png)
## Download
| Version        | URL           | QR Code  |
| :-----: |:------------| :---:|
| 1.0| [download](https://github.com/misakuo/Dream-Catcher/releases/download/release-1.0/dream_catcher-1.0.apk) | ![1481604756.png](https://ooo.0o0.ooo/2016/12/13/584f7ea6f343f.png) |
## Acknowledgments
[Stetho](https://github.com/facebook/stetho)    
[browsermob-proxy](https://github.com/lightbody/browsermob-proxy)    
[android-morphing-button](https://github.com/dmytrodanylyk/android-morphing-button)
