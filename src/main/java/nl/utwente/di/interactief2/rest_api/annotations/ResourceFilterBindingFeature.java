package nl.utwente.di.interactief2.rest_api.annotations;

import jakarta.ws.rs.container.DynamicFeature;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.FeatureContext;
import jakarta.ws.rs.ext.Provider;

@Provider
public class ResourceFilterBindingFeature {
//implements DynamicFeature
  //  @Override
    public void configure(ResourceInfo resourceInfo, FeatureContext context) {
        if (resourceInfo.getResourceMethod().isAnnotationPresent(LoggedIn.class)) {
            context.register(LoggedInFilter.class);
        }
        if (resourceInfo.getResourceMethod().isAnnotationPresent(Admin.class)) {
            context.register(AdminFilter.class);
        }
        if (resourceInfo.getResourceMethod().isAnnotationPresent(Participant.class)) {
            context.register(ParticipantFilter.class);
        }


    }
}