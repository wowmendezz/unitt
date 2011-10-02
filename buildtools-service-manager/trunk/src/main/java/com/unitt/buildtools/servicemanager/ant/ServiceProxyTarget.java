/*
 *  Licensed to UnitT Software, Inc. under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package com.unitt.buildtools.servicemanager.ant;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Execute;
import org.apache.tools.ant.taskdefs.LogStreamHandler;
import org.apache.tools.ant.taskdefs.MatchingTask;
import org.apache.tools.ant.taskdefs.condition.Os;
import org.apache.tools.ant.types.Commandline;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.Reference;
import org.apache.tools.ant.util.FileUtils;

import com.unitt.buildtools.servicemanager.ServiceProxyFactory;

public class ServiceProxyTarget extends MatchingTask
{
    public static final int COMMAND_LINE_LIMIT = 4096;
    public static final String EXECUTABLE = "apt";
    
    protected static final FileUtils FILE_UTILS = FileUtils.getFileUtils();

    protected File output;
    protected Path source;
    protected Path classpath;

    public ServiceProxyTarget()
    {
        classpath = createClasspath();
    }

    @Override
    public void execute() throws BuildException
    {
        validateAttributes();
        compile(getSourceFiles());
    }

    protected void validateAttributes()
    {
        if (output == null || !output.exists())
        {
            throw new BuildException("You must specify an existing output directory using the \"output\" attribute");
        }
        if (source == null)
        {
            throw new BuildException("You must specify an existing source directory using the \"source\" attribute");
        }
    }

    protected boolean compile(List<String> aFiles)
    {
        // Setup the apt executable
        Commandline cmd = new Commandline();
        cmd.setExecutable(EXECUTABLE);
        setupAptCommandlineSwitches(cmd);
        int firstFileName = cmd.size();
        
        //add the classpath
        cmd.createArgument().setValue("-classpath");
        cmd.createArgument().setPath(getClasspath());

        // add the files
        for (String file : aFiles)
        {
            cmd.createArgument().setValue(file);
        }

        // run
        return 0 == executeExternalCommand(cmd.getCommandline(), firstFileName, true);
    }

    protected int executeExternalCommand(String[] args, int firstFileName, boolean quoteFiles)
    {
        String[] commandArray = null;
        File tmpFile = null;

        try
        {
            /*
             * Many system have been reported to get into trouble with long
             * command lines - no, not only Windows ;-).
             * 
             * POSIX seems to define a lower limit of 4k, so use a temporary
             * file if the total length of the command line exceeds this limit.
             */
            if (Commandline.toString(args).length() > COMMAND_LINE_LIMIT && firstFileName >= 0)
            {
                PrintWriter out = null;
                try
                {
                    tmpFile = FILE_UTILS.createTempFile("files", "", null, true);
                    out = new PrintWriter(new FileWriter(tmpFile));
                    for (int i = firstFileName; i < args.length; i++)
                    {
                        if (quoteFiles && args[i].indexOf(" ") > -1)
                        {
                            args[i] = args[i].replace(File.separatorChar, '/');
                            out.println("\"" + args[i] + "\"");
                        }
                        else
                        {
                            out.println(args[i]);
                        }
                    }
                    out.flush();
                    commandArray = new String[firstFileName + 1];
                    System.arraycopy(args, 0, commandArray, 0, firstFileName);
                    commandArray[firstFileName] = "@" + tmpFile;
                }
                catch (IOException e)
                {
                    throw new BuildException("Error creating temporary file", e, getLocation());
                }
                finally
                {
                    FileUtils.close(out);
                }
            }
            else
            {
                commandArray = args;
            }

            try
            {
                Execute exe = new Execute(new LogStreamHandler(this, Project.MSG_INFO, Project.MSG_WARN));
                if (Os.isFamily("openvms"))
                {
                    // Use the VM launcher instead of shell launcher on VMS
                    // for java
                    exe.setVMLauncher(true);
                }
                exe.setAntRun(getProject());
                exe.setWorkingDirectory(getProject().getBaseDir());
                exe.setCommandline(commandArray);
                exe.execute();
                return exe.getExitValue();
            }
            catch (IOException e)
            {
                throw new BuildException("Error running " + args[0] + " compiler", e, getLocation());
            }
        }
        finally
        {
            if (tmpFile != null)
            {
                tmpFile.delete();
            }
        }
    }


    protected void setupAptCommandlineSwitches(Commandline aCmd)
    {
        aCmd.createArgument().setValue("-nocompile");
        aCmd.createArgument().setValue("-s");
        aCmd.createArgument().setValue(getOutput().getAbsolutePath());
        aCmd.createArgument().setValue("-factory");
        aCmd.createArgument().setValue(ServiceProxyFactory.class.getName());
    }

    protected List<String> getSourceFiles()
    {
        List<String> files = new ArrayList<String>();

        String[] srcList = source.list();
        if (srcList != null)
        {
            for (int i = 0; i < srcList.length; i++)
            {
                File srcDir = getProject().resolveFile(srcList[i]);

                // verify file
                if (srcDir == null || !srcDir.exists())
                {
                    throw new BuildException("source directory \"" + srcDir.getPath() + "\" does not exist.");
                }

                // grab included files
                DirectoryScanner ds = getDirectoryScanner(srcDir);
                String[] includes = ds.getIncludedFiles();
                if (includes != null)
                {
                    files.addAll(getSourceFiles(srcDir, includes));
                }
            }
        }

        return files;
    }
    
    protected List<String> getSourceFiles(File aDirectory, String[] aIncludes)
    {
        List<String> files = new ArrayList<String>();
        
        for (int i = 0; i < aIncludes.length; i++)
        {
            if (aIncludes[i].endsWith(".java"))
            {
                File mFile = new File(aDirectory.getAbsolutePath() + File.separator + aIncludes[i]);
                if (mFile.exists())
                {
                    files.add(mFile.getAbsolutePath());
                }
            }
        }
        
        return files;
    }

    public File getOutput()
    {
        return output;
    }

    public void setOutput(File aOutput)
    {
        output = aOutput;
    }

    public Path getSource()
    {
        return source;
    }

    public void setSource(Path aSource)
    {
        source = aSource;
    }
    
    public Path createClasspath()
    {
        if (classpath == null)
        {
            classpath = new Path(getProject());
        }
        
        return classpath.createPath();
    }
    
    public Path getClasspath()
    {
        return classpath;
    }
    
    public void setClasspath(Path aClasspath)
    {
        classpath = aClasspath;
    }
    
    public void setClasspathRef(Reference aClasspath)
    {
        createClasspath().setRefid(aClasspath);
    }
}
