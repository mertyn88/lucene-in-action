package lucene.search;

import java.io.IOException;
import java.nio.file.Paths;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class Searcher {

    public static void search(String indexPath, String input) throws IOException, ParseException {
        //변경코드 IndexReader 추가
        Directory indexDir = FSDirectory.open(Paths.get(indexPath));
        IndexReader indexReader = DirectoryReader.open(indexDir);
        IndexSearcher indexSearcher = new IndexSearcher(indexReader);

        QueryParser queryParser = new QueryParser("contentsFile", new StandardAnalyzer());
        Query query = queryParser.parse(input);

        TopDocs hits = indexSearcher.search(query, 10);
        System.out.println(hits.totalHits);
        System.out.println("Parse Query >> "+query.toString());

        for(ScoreDoc scoreDoc : hits.scoreDocs){
            Document doc = indexSearcher.doc(scoreDoc.doc);

            System.out.println(doc.get("id"));
            System.out.println(doc.get("contentsFile"));
            System.out.println(doc.get("contentsString"));
            System.out.println(doc.get("filename"));
            System.out.println("------------");
        }
    }

    public static void main(String[] args) throws IOException, ParseException {
        String indexDir = "/data/test/index_data";       // 색인된 디렉토리 위치
        String input = "개행이";

        search(indexDir, input);
    }


}
