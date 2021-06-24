/*
 * Copyright 2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.api.tasks.javadoc;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.Path;
import org.gradle.workers.WorkAction;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public abstract class GenerateGroovydoc implements WorkAction<GroovydocParameters> {
    @Override
    public void execute() {
        GroovydocParameters parameters = getParameters();
        try {
            invokeGroovyDoc(parameters);
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            throw new RuntimeException("Could not generate groovydoc", e);
        }
    }

    private void invokeGroovyDoc(GroovydocParameters parameters) throws ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        Class<?> groovyDocClass = Thread.currentThread().getContextClassLoader().loadClass("org.codehaus.groovy.ant.Groovydoc");
        Method execute = groovyDocClass.getMethod("execute");
        Object groovydoc = groovyDocClass.getDeclaredConstructor().newInstance();

        Project project = new Project();
        project.init();
        Path path = new Path(project);
        path.setPath(parameters.getTemporaryDir().getAsFile().get().toString());

        Method setSourcepath = groovyDocClass.getMethod("setSourcepath", Path.class);
        setSourcepath.invoke(groovydoc, path);

        Method setProject = groovyDocClass.getMethod("setProject", Project.class);
        setProject.invoke(groovydoc, project);

        Method setDestdir = groovyDocClass.getMethod("setDestdir", File.class);
        setDestdir.invoke(groovydoc, parameters.getDestinationDir().getAsFile().get());

        Method setUse = groovyDocClass.getMethod("setUse", boolean.class);
        setUse.invoke(groovydoc, parameters.getIsUse().get());

        Method setNoTimestamp = groovyDocClass.getMethod("setNoTimestamp", boolean.class);
        setNoTimestamp.invoke(groovydoc, parameters.getIsNoTimestamp().get());

        Method setPackagenames = groovyDocClass.getMethod("setPackagenames", String.class);
        setPackagenames.invoke(groovydoc, "**.*");

        if(parameters.getWindowTitle().isPresent()) {
            Method setWindowtitle = groovyDocClass.getMethod("setWindowtitle", String.class);
            setWindowtitle.invoke(groovydoc, parameters.getWindowTitle().get());
        }

        if(parameters.getDocTitle().isPresent()) {
            Method setDoctitle = groovyDocClass.getMethod("setDoctitle", String.class);
            setDoctitle.invoke(groovydoc, parameters.getDocTitle().get());
        }

        if(parameters.getHeader().isPresent()) {
            Method setHeader = groovyDocClass.getMethod("setHeader", String.class);
            setHeader.invoke(groovydoc, parameters.getHeader().get());
        }

        if(parameters.getFooter().isPresent()) {
            Method setFooter = groovyDocClass.getMethod("setFooter", String.class);
            setFooter.invoke(groovydoc, parameters.getFooter().get());
        }

        if(parameters.getPathToOverview().isPresent()) {
            Method setOverview = groovyDocClass.getMethod("setOverview", File.class);
            setOverview.invoke(groovydoc, new File(parameters.getPathToOverview().get()));
        }

        Method setPrivate = groovyDocClass.getMethod("setPrivate", boolean.class);
        setPrivate.invoke(groovydoc, parameters.getIsNoTimestamp().get());


        execute.invoke(groovydoc);
    }
}
