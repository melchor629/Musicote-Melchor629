console.log('This would be the main JS file.');
var ua = navigator.userAgent.toLowerCase();
var isAndroid = ua.indexOf("android") > -1;
if(isAndroid) {
 window.location.href = 'http://reinoslokos.no-ip.org/com.melchor629.musicote.apk';
}
