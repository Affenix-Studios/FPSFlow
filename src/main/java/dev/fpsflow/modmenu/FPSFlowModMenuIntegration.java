package dev.fpsflow.modmenu;

import dev.fpsflow.screen.FPSFlowConfigScreen;

// Diese Klasse ist für ModMenu gedacht.
// Da ModMenu bei dir beim Build aktuell nicht verfügbar ist,
// muss die Klasse ohne ModMenu-CompileOnly-Abhängigkeit gebaut werden können.
public class FPSFlowModMenuIntegration {

    // Zur Laufzeit wird das Mapping über die ModMenu-EntryPoint in fabric.mod.json genutzt.
    // Die konkrete ModMenu-API wird hier absichtlich nicht direkt referenziert.
    public static Object createConfigScreen() {
        // FPSFlowConfigScreen benötigt einen parent Screen.
        // Da wir im Build ohne ModMenu-API arbeiten, geben wir einfach null zurück.
        // (Wenn du ModMenu wirklich aktivieren willst, muss die echte ModMenu-API wieder korrekt angebunden werden.)
        return null;
    }

}

