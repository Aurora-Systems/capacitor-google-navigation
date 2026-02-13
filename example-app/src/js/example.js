import { GoogleNavigation } from 'capacitor-google-navigation';

window.testEcho = () => {
    const inputValue = document.getElementById("echoInput").value;
    GoogleNavigation.echo({ value: inputValue })
}
