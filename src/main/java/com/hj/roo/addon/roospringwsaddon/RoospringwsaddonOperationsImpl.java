package com.hj.roo.addon.roospringwsaddon;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.Validate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.springframework.roo.classpath.TypeLocationService;
import org.springframework.roo.classpath.TypeManagementService;
import org.springframework.roo.classpath.details.MemberFindingUtils;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetailsBuilder;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadataBuilder;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.*;
import org.springframework.roo.support.util.XmlUtils;
import org.w3c.dom.Element;

/**
 * Implementation of operations this add-on offers.
 *
 * @since 1.1
 */
@Component // Use these Apache Felix annotations to register your commands class in the Roo container
@Service
public class RoospringwsaddonOperationsImpl implements RoospringwsaddonOperations {
    
    /**
     * Use ProjectOperations to install new dependencies, plugins, properties, etc into the project configuration
     */
    @Reference private ProjectOperations projectOperations;

    /**
     * Use TypeLocationService to find types which are annotated with a given annotation in the project
     */
    @Reference private TypeLocationService typeLocationService;
    
    /**
     * Use TypeManagementService to change types
     */
    @Reference private TypeManagementService typeManagementService;

    /** {@inheritDoc} */
    public boolean isCommandAvailable() {
        // Check if a project has been created
        return projectOperations.isFocusedProjectAvailable();
    }

    /** {@inheritDoc} */
    public void annotateType(JavaType javaType) {
        // Use Roo's Assert type for null checks
        Validate.notNull(javaType, "Java type required");

        // Obtain ClassOrInterfaceTypeDetails for this java type
        ClassOrInterfaceTypeDetails existing = typeLocationService.getTypeDetails(javaType);

        // Test if the annotation already exists on the target type
        if (existing != null && MemberFindingUtils.getAnnotationOfType(existing.getAnnotations(), new JavaType(RooRoospringwsaddon.class.getName())) == null) {
            ClassOrInterfaceTypeDetailsBuilder classOrInterfaceTypeDetailsBuilder = new ClassOrInterfaceTypeDetailsBuilder(existing);
            
            // Create JavaType instance for the add-ons trigger annotation
            JavaType rooRooRoospringwsaddon = new JavaType(RooRoospringwsaddon.class.getName());

            // Create Annotation metadata
            AnnotationMetadataBuilder annotationBuilder = new AnnotationMetadataBuilder(rooRooRoospringwsaddon);
            
            // Add annotation to target type
            classOrInterfaceTypeDetailsBuilder.addAnnotation(annotationBuilder.build());
            
            // Save changes to disk
            typeManagementService.createOrUpdateTypeOnDisk(classOrInterfaceTypeDetailsBuilder.build());
        }
    }

    /** {@inheritDoc} */
    public void annotateAll() {
        // Use the TypeLocationService to scan project for all types with a specific annotation
        for (JavaType type: typeLocationService.findTypesWithAnnotation(new JavaType("org.springframework.roo.addon.javabean.RooJavaBean"))) {
            annotateType(type);
        }
    }
    
    /** {@inheritDoc} */
    public void setup() {
        // Install the add-on Google code repository needed to get the annotation 
        projectOperations.addRepository("", new Repository("Roospringwsaddon Roo add-on repository", "Roospringwsaddon Roo add-on repository", "https://springws-addon.googlecode.com/svn/repo"));
        
        List<Dependency> dependencies = new ArrayList<Dependency>();
        
        // Install the dependency on the add-on jar (
        dependencies.add(new Dependency("com.hj.roo.addon.roospringwsaddon", "com.hj.roo.addon.roospringwsaddon", "0.1.0.BUILD-SNAPSHOT", DependencyType.JAR, DependencyScope.PROVIDED));
        
        // Install dependencies defined in external XML file

        Element configuration = XmlUtils.getConfiguration(getClass());
        for(Element depdendencyElement : XmlUtils.findElements(
                "/configuration/dependencies/dependency",
                configuration)) {
            dependencies.add(new Dependency(depdendencyElement));
        }


        // Add all new dependencies to pom.xml
        projectOperations.addDependencies("", dependencies);

        List<Plugin> plugins = new ArrayList<Plugin>();

        for(Element pluginElement : XmlUtils.findElements(
            "/configuration/build/plugins/plugin",
            configuration)) {
            plugins.add(new Plugin(pluginElement));
        }


        projectOperations.addBuildPlugins("", plugins);

    }
}