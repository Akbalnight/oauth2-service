const urlParams = new URLSearchParams(window.location.search);

console.log("main JS", urlParams.has('error'));

if(urlParams.has('error')){
    document.getElementById("error-block").classList.add('d-block');
    document.getElementById("logout-block").classList.add('d-none');
}

if(urlParams.has('logout')){
    document.getElementById("error-block").classList.add('d-none');
    document.getElementById("logout-block").classList.add('d-block');
}