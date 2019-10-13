package edu.usf.csee.cereal.evotransform;

import com.google.inject.Singleton;

import org.metaborg.core.project.IProjectService;
import org.metaborg.core.project.SimpleProjectService;
import org.metaborg.spoofax.core.SpoofaxModule;

public class CustomInjectModule extends SpoofaxModule {

    @Override 
    protected void bindProject() {
        bind(IProjectService.class)
            .to(SimpleProjectService.class)
            .in(Singleton.class);        
    }
    // @Override
    // protected void configure() {
    //     super.configure();

    //     // MapBinder
    //     //     .newMapBinder(binder(), key-class, IProjectService.class)
    //         // .addBinding(key)
    //         // .to(SimpleProjectService.class)
    //         // .in(Singleton.class);            
    // }
}