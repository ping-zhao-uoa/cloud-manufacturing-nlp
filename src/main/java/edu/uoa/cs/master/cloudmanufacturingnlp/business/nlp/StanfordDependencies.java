/*
 * Copyright (c) 2015 The University of Auckland
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
 *
 * Authors: Ping Zhao (pzha291@aucklanduni.ac.nz)
 * Created: 2015-06-10
 * Last Updated: --
 */
package edu.uoa.cs.master.cloudmanufacturingnlp.business.nlp;

import java.io.StringReader;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.process.Tokenizer;
import edu.stanford.nlp.process.TokenizerFactory;
import edu.stanford.nlp.trees.EnglishGrammaticalStructure;
import edu.stanford.nlp.trees.GrammaticalStructureFactory;
import edu.stanford.nlp.trees.PennTreebankLanguagePack;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreebankLanguagePack;
import edu.stanford.nlp.trees.TypedDependency;
import edu.uoa.cs.master.cloudmanufacturingnlp.util.Constants;

public class StanfordDependencies {
	private TreebankLanguagePack tlp = new PennTreebankLanguagePack();
	private GrammaticalStructureFactory gsf = tlp.grammaticalStructureFactory();
	private TokenizerFactory<? extends HasWord> tokenizerFactory = tlp.getTokenizerFactory();

	private LexicalizedParser lp = LexicalizedParser.loadModel("edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz",
			"-maxLength", "80", "-retainTmpSubcategories");

	/** singleton */
	private static StanfordDependencies instance = new StanfordDependencies();

	/**
	 * singleton
	 */
	private StanfordDependencies() {
		// do nothing;
	}

	public static StanfordDependencies getInstance() {
		return instance;
	}

	/**
	 * Parse the input raw sentence, output the Stanford Dependencies.
	 * <p>
	 * An output example is:shares-3={companyC-2=nsubj}
	 * </p>
	 * 
	 * @param triples
	 * @param naturalLanguageRule
	 * @return
	 */
	public String parseNaturalLanguage(Map<String, Map<String, String>> triples, String naturalLanguageRule) {
		String action = null;

		Tokenizer<? extends HasWord> toke = tokenizerFactory.getTokenizer(new StringReader(naturalLanguageRule));
		List<? extends HasWord> sentence = toke.tokenize();

		Tree parse = lp.parse(sentence);
		EnglishGrammaticalStructure gs = (EnglishGrammaticalStructure) gsf.newGrammaticalStructure(parse);

		Collection<TypedDependency> tdl = gs.typedDependencies();

		for (TypedDependency dependency : tdl) {
			String gov = dependency.gov().toString();
			String reln = dependency.reln().toString();
			String dep = dependency.dep().toString();

			if (triples.containsKey(gov)) {
				triples.get(gov).put(dep, reln);
			} else {
				Map<String, String> triple = new HashMap<String, String>();

				triple.put(dep, reln);
				triples.put(gov, triple);
			}

			if (reln.equalsIgnoreCase(Constants.Nlp.NSUBJ)) {
				action = gov;
			}
		}

		return action;
	}

	public static void main(String[] args) {
		Map<String, Map<String, String>> triples = new HashMap<String, Map<String, String>>();

		// StanfordDependencies.getInstance().parseNaturalLanguage(
		// triples, "The companyA shares resources within its own company.");
		//
		// StanfordDependencies.getInstance().parseNaturalLanguage(
		// triples, "The companyB shares resources with NZ-based companies.");
		//
		StanfordDependencies.getInstance().parseNaturalLanguage(triples,
				"The companyC shares soft resources within the public cloud;");
		//
		// StanfordDependencies
		// .getInstance()
		// .parseNaturalLanguage(
		// triples,
		// "The companyC shares hard resources with specific companies, i.e., companyA, companyB and companyE;");
		//
		// StanfordDependencies
		// .getInstance()
		// .parseNaturalLanguage(
		// triples,
		// "The companyD shares machining resources with companies having an credit rating higher than 8.0 (out of 10.0);");
		//
		// StanfordDependencies.getInstance().parseNaturalLanguage(
		// triples, "The companyE shares OKUMA MP-46V within the public cloud.");
		//
		// StanfordDependencies
		// .getInstance()
		// .parseNaturalLanguage(
		// triples,
		// "The companyF shares resources with private limited companies in operation for more than 10 years.");

		System.out.println(triples);
	}
}
