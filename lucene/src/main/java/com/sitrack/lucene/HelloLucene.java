package com.sitrack.lucene;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;

public class HelloLucene {

	public static void main(String[] args) throws IOException, ParseException {
		// 0. Specify the analyzer for tokenizing text.
		// The same analyzer should be used for indexing and searching
		StandardAnalyzer analyzer = new StandardAnalyzer();

		// 1. create the index
		Directory index = new RAMDirectory();

		IndexWriterConfig config = new IndexWriterConfig(analyzer);

		IndexWriter w = new IndexWriter(index, config);
		addDoc(w, "Lucene in Action", "193398817");
		addDoc(w, "Lucene para Dummies", "55320055Z");
		addDoc(w, "Managing Gigabytes", "55063554A");
		addDoc(w, "The Art of Computer Science", "9900333X");
		addDoc(w, "En la cima de la montaña", "9945333X");
		w.close();

		String inputText;
		Scanner reader = new Scanner(System.in);  // Reading from System.in;
		
		while(true){
			//User input
			System.out.println("Enter a text to search: ");
			inputText = reader.nextLine();
			if(inputText.equals("--q")) break;

			// the "title" arg specifies the default field to use
			// when no field is explicitly specified in the query.
			Query q = new QueryParser("title", analyzer).parse(inputText);
			
			List<Document> documents = search(q, index);
			displayResults(documents);
		}

		// reader can only be closed when there
		// is no need to access the documents any more.
		System.out.println("Programa finalizado");
		reader.close();
	}
	
	private static List<Document> search(Query q, Directory index) throws IOException{
		int hitsPerPage = 10;
        IndexReader reader = DirectoryReader.open(index);
        IndexSearcher searcher = new IndexSearcher(reader);
		TopScoreDocCollector collector = TopScoreDocCollector.create(hitsPerPage);
		searcher.search(q, collector);
        ScoreDoc[] hits = collector.topDocs().scoreDocs;

		List<Document> documents = new ArrayList<Document>(0);

        for (ScoreDoc hit : hits) {
            int docId = hit.doc;
            documents.add(searcher.doc(docId));
        }

		return documents;
	}
	
	private static void displayResults(List<Document> documents) throws IOException{
		System.out.println("Found " + documents.size() + " hits.");
        int index = 1;
        for (Document doc : documents) {
			System.out.println((index) + ". " + doc.get("isbn") + "\t" + doc.get("title"));
		}
	}

	private static void addDoc(IndexWriter w, String title, String isbn) throws IOException {
		Document doc = new Document();
		doc.add(new TextField("title", title, Field.Store.YES));

		// use a string field for isbn because we don't want it tokenized
		doc.add(new StringField("isbn", isbn, Field.Store.YES));
		w.addDocument(doc);
	}
}
