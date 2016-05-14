# Architecture-Relation-Evaluator


Both Populator and Diff require the emf jars from the equivalent eclipse instance to run.

Example rsf files are presented in the Examples folder. The output files of the populator are the required input for the diff.

Populator:
java -cp populator.jar:model.jar:eclipse/plugins/org.eclipse.emf.common_xxx.jar:eclipse/plugins/org.eclipse.emf.ecore_xxx.jar:eclipse/plugins/org.eclipse.emf.ecore.xmi_xxx.jar step4.Populator <systemName> <names.rsf file> <relations Folder> <results folder> <extraRelations.rsf file> <extraProperties.rsf file> <ExtraConnectors.rsf file>


Diff:
java -cp diff.jar:model.jar:eclipse/plugins/org.eclipse.emf.common_xxx.jar:eclipse/plugins/org.eclipse.emf.ecore_xxx.jar:eclipse/plugins/org.eclipse.emf.ecore.xmi_xxx.jar step5.Diff3 <goal System xml> <tested Systems Folder>
