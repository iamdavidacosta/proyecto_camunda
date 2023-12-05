package com.camunda;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

import java.io.IOException;
import java.io.InputStream;

public class estadoPersonas implements JavaDelegate {

    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {
        // Obtener la variable 'cedula' del modelo
        String cedula = (String) delegateExecution.getVariable("cedula");

        // Verificar que 'cedula' no sea nula
        if (cedula == null) {
            // Manejar el caso cuando 'cedula' es nula
            delegateExecution.setVariable("esPersonaValida", "No");
            return;
        }

        // Verificar la cedula en el JSON
        boolean esPersonaValida = verificarPersona(cedula);

        // Establecer el resultado en una variable del proceso
        delegateExecution.setVariable("esPersonaValida", esPersonaValida ? "Si" : "No");
    }

    private boolean verificarPersona(String cedula) {
        // Cargar el JSON desde un archivo o recurso (asegúrate de tener el archivo JSON en tu proyecto)
        InputStream jsonStream = getClass().getClassLoader().getResourceAsStream("informacion_personas.json");

        // Verificar que el JSON se cargue correctamente
        if (jsonStream == null) {
            // Manejar el caso cuando no se puede cargar el JSON
            return false;
        }

        // Convertir el JSON a un array de objetos JsonNode
        JsonNode[] personas = null;
        try {
            personas = new ObjectMapper().readValue(jsonStream, JsonNode[].class);
        } catch (IOException e) {
            e.printStackTrace();  // Manejar la excepción
            return false;
        }

        // Buscar la cedula en el array de personas y verificar las condiciones
        if (personas != null) {
            for (JsonNode persona : personas) {
                if (persona.has("cedula") && persona.get("cedula").asText().equals(cedula)) {
                    return persona.has("libre_de_multa") && persona.get("libre_de_multa").asBoolean()
                            && persona.has("inscrito_en_rut") && persona.get("inscrito_en_rut").asBoolean()
                            && persona.has("documento_valido") && persona.get("documento_valido").asBoolean();
                }
            }
        }

        return false; // Si no se encuentra la cedula en el JSON o si hay algún problema en la estructura del JSON
    }
}