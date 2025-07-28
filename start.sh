#!/bin/bash

# Remplace ce chemin par le chemin réel vers ton mvn local
MAVEN="./apache-maven-3.9.6/bin/mvn"

echo "🔄 Nettoyage et compilation du projet..."
$MAVEN clean install -DskipTests

if [ $? -ne 0 ]; then
  echo "❌ La compilation a échoué. Arrêt du script."
  exit 1
fi

echo "✅ Compilation réussie. Démarrage de l'application..."
$MAVEN spring-boot:run
