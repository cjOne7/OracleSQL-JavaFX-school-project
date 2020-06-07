package sample.controllers.adminscontrollers.studymaterials;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ScrollPane;
import javafx.scene.text.Text;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import sample.databasemanager.DbManager;
import sample.enums.StudyMatColumns;
import sample.enums.StylesEnum;

import java.io.*;
import java.net.URL;
import java.sql.Blob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Scanner;

public class StudyMaterialController implements Initializable {

    private final DbManager dbManager = new DbManager();

    private File file;
    private String fileType;

    @FXML
    private ScrollPane scrollPane;
    @FXML
    private Text text;

    @Override
    public void initialize(final URL location, final ResourceBundle resources) {
        scrollPane.setStyle(StylesEnum.PANE_STYLE.getStyle());

        final String selectQuery = "SELECT FILE_NAME, THE_FILE, FILE_TYPE FROM ST58310.STY_MTRL WHERE STUDY_MATERIAL_ID = ?";
        try {
            final PreparedStatement preparedStatement = dbManager.getConnection().prepareStatement(selectQuery);
            preparedStatement.setInt(1, CreateStudyMaterialsController.studyMatId);
            final ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                final String fileName = resultSet.getString(StudyMatColumns.FILE_NAME.getColumnName());
                fileType = resultSet.getString(StudyMatColumns.FILE_TYPE.getColumnName());
                final Blob blob = resultSet.getBlob(StudyMatColumns.THE_FILE.getColumnName());
                file = getFile(blob, fileName, fileType);
            }
            switch (fileType.toLowerCase().trim()) {
                case "txt":
                    readTxtFile();
                    break;
                case "pdf":
                    readPdfFile();
                    break;
                case "doc":
                case "docx":
                    readDocFile();
                    break;
            }
            file.deleteOnExit();
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
    }

    private File getFile(final Blob blob, final String fileName, final String fileType) throws SQLException, IOException {
        if (blob != null) {
            final InputStream input = blob.getBinaryStream();
            final String filePath = System.getProperty("user.home") + "/Downloads/" + fileName + '.' + fileType;
            final File file = new File(filePath);
            final OutputStream fos = new FileOutputStream(file);
            final byte[] buffer = new byte[1024];
            while (input.read(buffer) > 0) {
                fos.write(buffer);
            }
            return file;
        } else {
            return null;
        }
    }

    public void readTxtFile() throws IOException {
        final StringBuilder stringBuilder = new StringBuilder();
        final Scanner sc = new Scanner(file);
        while (sc.hasNextLine()) {
            stringBuilder.append(sc.nextLine()).append("\n");
        }
        text.setText(stringBuilder.toString());

        sc.close();
    }

    public void readDocFile() throws IOException {
        final InputStream fis = new FileInputStream(file.getAbsolutePath());

        final XWPFDocument document = new XWPFDocument(fis);
        final List<XWPFParagraph> paragraphs = document.getParagraphs();

        final StringBuilder stringBuilder = new StringBuilder();
        for (XWPFParagraph para : paragraphs) {
            stringBuilder.append(para.getText()).append("\n");
        }
        text.setText(stringBuilder.toString());

        fis.close();
    }

    public void readPdfFile() throws IOException {
        final PDDocument document = PDDocument.load(file);
        if (!document.isEncrypted()) {
            final PDFTextStripper stripper = new PDFTextStripper();
            text.setText(stripper.getText(document));
        }
        document.close();
    }
}
