package fr.inria.autojmh.instrument;

import fr.inria.autojmh.generators.AJMHGenerator;
import fr.inria.autojmh.generators.BenchmarkTest;
import fr.inria.autojmh.selection.SnippetSelector;
import fr.inria.autojmh.selection.TaggedStatementDetector;
import fr.inria.autojmh.selection.Tagglet;
import fr.inria.autojmh.snippets.BenchSnippet;
import fr.inria.autojmh.snippets.TemplateInputVariable;
import fr.inria.autojmh.tool.AJMHConfiguration;
import fr.inria.diversify.syringe.SpoonMetaFactory;
import org.junit.Test;
import spoon.processing.ProcessingManager;
import spoon.reflect.code.CtLoop;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.factory.Factory;
import spoon.support.QueueProcessingManager;

import java.net.URISyntaxException;
import java.util.List;

import static fr.inria.autojmh.selection.TaggletStatementDetectorTest.CLASS_NAME;
import static fr.inria.autojmh.selection.TaggletStatementDetectorTest.getTaggletsList;
import static fr.inria.autojmh.selection.TaggletStatementDetectorTest.process;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

public class BenchSnippetTest extends BenchmarkTest {


}