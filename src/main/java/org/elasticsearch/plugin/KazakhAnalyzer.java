package org.elasticsearch.plugin;

import org.apache.lucene.analysis.*;
import org.apache.lucene.analysis.standard.StandardTokenizer;

// Код взят отсюда
//https://github.com/apache/lucene/blob/main/lucene/analysis/common/src/java/org/apache/lucene/analysis/ru/RussianAnalyzer.java
public class KazakhAnalyzer extends Analyzer {
    @Override
    protected TokenStreamComponents createComponents(String fieldName) {

        final Tokenizer source = new StandardTokenizer();
        TokenStream result = new LowerCaseFilter(source);
        result = new KazakhStemmerTokenFilter(result);

        return new TokenStreamComponents(source, result);
    }

    @Override
    protected TokenStream normalize(String fieldName, TokenStream in) {
        TokenStream result = new LowerCaseFilter(in);
        result = new LowerCaseFilter(result);
        return result;
    }
}
