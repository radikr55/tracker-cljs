const electron = require('electron');

var ipc = electron.ipcRenderer;

document.querySelector('body').addEventListener('click', event => {
    ipc.send('notification-close', {});
});

ipc.on("switch-theme", function (event, data) {
    document.getElementById('html-header').className = data;
});


var url = new URLSearchParams(window.location.search);
var time = url.get("time");
var theme = url.get("theme");

document.getElementById('notification').innerHTML = "You were not present for "  + time + " minutes";
document.getElementById('html-header').className = theme;
