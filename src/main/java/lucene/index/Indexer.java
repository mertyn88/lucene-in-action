package lucene.index;

import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;

import lucene.enums.IndexType;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.FileUtils;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
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

    /* Index File loop */
    public int index(IndexType indexType, Term term, String dataDir, FileFilter filter) throws Exception {
        File[] files = new File(dataDir).listFiles();
        for(int idx = 0; idx < files.length; idx++) {
            File file = files[idx];
            if(!file.isDirectory() && !file.isHidden() && file.exists() && file.canRead() && (filter == null || filter.accept(file))){
                System.out.println("Indexing " + file.getCanonicalPath());
                switch (indexType){
                    case INDEX_CREATE:
                        indexCreate(getDocument(file, idx)); break;
                    case INDEX_UPDATE:
                        indexUpdate(getDocument(file, idx), term); break;
                    default:
                }
            }
        }
        //변경 코드 indexerWriter.numDocs() -> indexerWriter.getDocStats().numDocs
        return indexWriter.getDocStats().numDocs;
    }

    /* Index Update */
    public long indexUpdate(Document document, Term term) throws Exception {
        return indexWriter.updateDocument(term, document);
    }

    /* Index Delete */
    public long indexDelete(Term term) throws Exception {
        // indexWriter.getDocStats().numDocs;
        return indexWriter.deleteDocuments(term);
    }

    /* Index Create */
    public long indexCreate(Document document) throws Exception {
        return indexWriter.addDocument(document);
    }

    /* Index Document Set */
    public Document getDocument(File file, int id) throws Exception {
        Document doc = new Document();
        //변경코드 new Field() -> new TextField() -> new TextField로 색인시 검색결과를 가져올 수 없다!! un-store하다..
        //변경코드 Field.Index.NOT_ANALYZED -> 삭제됨
        doc.add(new StringField("id", Integer.toString(id), Field.Store.YES));
        doc.add(new TextField("contentsFile", new FileReader(file)));
        doc.add(new StringField("contentsString", FileUtils.readFileToString(file, StandardCharsets.UTF_8), Field.Store.YES));
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
        long count = 0;
        try{
            /* Index Document */
            count = indexer.index(IndexType.INDEX_CREATE, null, dataDir, new TextFilesFilter());
            /* Index Update */
            count = indexer.index(IndexType.INDEX_UPDATE, new Term("id", "1"), dataDir, new TextFilesFilter());
            /* Index Delete */
            count = indexer.indexDelete(new Term("id", "0"));
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            indexer.close();
        }
        System.out.println("Indexing document " + count);
    }
}