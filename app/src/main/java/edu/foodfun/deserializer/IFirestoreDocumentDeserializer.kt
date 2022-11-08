package edu.foodfun.deserializer

import com.google.firebase.firestore.DocumentSnapshot

interface IFirestoreDocumentDeserializer<T> : IDeserializer<DocumentSnapshot, T>