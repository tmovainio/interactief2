package nl.utwente.di.interactief2.rest_api.annotations;

import jakarta.ws.rs.ext.Provider;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Provider
@Retention(RetentionPolicy.RUNTIME)
public @interface Participant {}
