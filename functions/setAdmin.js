const admin = require("firebase-admin");

// IMPORTANTE: Este script necesita el fichero de clave de servicio que descargarás en el siguiente paso.
// Asegúrate de que el fichero se llame 'serviceAccountKey.json' y esté en esta misma carpeta.
const serviceAccount = require("./serviceAccountKey.json");

admin.initializeApp({
  credential: admin.credential.cert(serviceAccount)
});

// El correo del usuario al que quieres dar rol de admin se pasará como argumento.
const userEmail = process.argv[2];

if (!userEmail) {
  console.error("Error: Por favor, proporciona el correo del usuario como argumento.");
  console.log("Ejemplo: node setAdmin.js tu-correo@ejemplo.com");
  process.exit(1);
}

console.log(`Buscando al usuario: ${userEmail}...`);

admin.auth().getUserByEmail(userEmail)
  .then((user) => {
    // Añadir la "etiqueta" (custom claim) de rol de administrador.
    console.log(`Asignando rol de 'admin' al UID: ${user.uid}...`);
    return admin.auth().setCustomUserClaims(user.uid, { role: 'admin' });
  })
  .then(() => {
    console.log(`¡Éxito! El usuario ${userEmail} ahora es un administrador.`);
    process.exit(0);
  })
  .catch((error) => {
    console.error("Error al asignar el rol de administrador:", error);
    process.exit(1);
  });
