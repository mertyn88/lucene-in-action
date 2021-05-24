package lucene.index;

import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class Indexer {

    private IndexWriter indexWriter;
    public Indexer(String indexDir) throws IOException {
        // 변경 코드 open(new File) -> open(Path)
        Directory dir = FSDirectory.open(Paths.get(indexDir));
        indexWriter = new IndexWriter(dir, new IndexWriterConfig(new StandardAnalyzer()));
    }

    public void close() throws IOException{
        indexWriter.close();
    }

    public int index(String dataDir, FileFilter filter) throws Exception {
        File[] files = new File(dataDir).listFiles();
        for(File file : files){
            if(!file.isDirectory() && !file.isHidden() && file.exists() && file.canRead() && (filter == null || filter.accept(file))){
                indexFile(file);
            }
        }
        //변경 코드 indexerWriter.numDocs() -> indexerWriter.getDocStats().numDocs
        return indexWriter.getDocStats().numDocs;
    }

    public void indexFile(File file) throws Exception {
        System.out.println("Indexing " + file.getCanonicalPath());
        Document doc = getDocument(file);
        indexWriter.addDocument(doc);
    }

    public Document getDocument(File file) throws Exception {
        Document doc = new Document();
        //변경코드 new Field() -> new TextField()
        //변경코드 Field.Index.NOT_ANALYZED -> 삭제됨
        doc.add(new TextField("contents", new FileReader(file)));
        doc.add(new StringField("filename", file.getName(), Field.Store.YES));

        return doc;
    }

    private static class TextFilesFilter implements FileFilter{
        @Override
        public boolean accept(File file) {
            return file.getName().toLowerCase().endsWith(".txt");
        }
    }

    public static void main(String[] args) throws Exception{
        String indexDir = "/data/test/index_data";       // 해당 디렉토리에 색인 파일 생성
        String dataDir = "/data/test";                   // 해당 디렉토리의 파일을 대상으로 색인 지정

        Indexer indexer = new Indexer(indexDir);
        int count = 0;
        try{
            count = indexer.index(dataDir, new TextFilesFilter());
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            indexer.close();
        }
        System.out.println("Indexing num " + count);
    }
}