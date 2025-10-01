package de.dnb.gnd.utils.isbd;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.util.Units;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.UnderlinePatterns;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFSettings;

public class WordFormatter {

	public static String FOLDER = "D:/Analysen/karg/NSW/test/";

	public static String logo = FOLDER + "Orgel Schwerin 2.jpg";
	public static String paragraph1 = FOLDER + "poi-word-para1.txt";
	public static String paragraph2 = FOLDER + "poi-word-para2.txt";
	public static String paragraph3 = FOLDER + "poi-word-para3.txt";
	public static String output = FOLDER + "rest-with-spring.docx";

	public static String convertTextFileToString(final String fileName) {
		try (Stream<String> stream = Files.lines(Paths.get(fileName))) {

			return stream.collect(Collectors.joining(" "));
		} catch (final IOException e) {
			return null;
		}
	}

	public static void main(final String[] args) throws URISyntaxException, InvalidFormatException, IOException {
		final XWPFDocument document = new XWPFDocument();
		final XWPFParagraph title = document.createParagraph();
		title.setAlignment(ParagraphAlignment.CENTER);

		final XWPFSettings settings = new XWPFSettings();

		final XWPFRun titleRun = title.createRun();
		titleRun.setText("Build Your REST API with Spring");
		titleRun.setColor("009933");
		titleRun.setBold(true);
		titleRun.setFontFamily("Courier");
		titleRun.setFontSize(20);

		final XWPFParagraph subTitle = document.createParagraph();
		subTitle.setAlignment(ParagraphAlignment.CENTER);

		final XWPFRun subTitleRun = subTitle.createRun();
		subTitleRun.setText("from HTTP fundamentals to API Mastery");
		subTitleRun.setColor("00CC44");
		subTitleRun.setFontFamily("Courier");
		subTitleRun.setFontSize(16);
		subTitleRun.setTextPosition(20);
		subTitleRun.setUnderline(UnderlinePatterns.DOT_DOT_DASH);

		final XWPFParagraph image = document.createParagraph();
		image.setAlignment(ParagraphAlignment.CENTER);

		final XWPFRun imageRun = image.createRun();
		imageRun.setTextPosition(20);
		final Path imagePath = Paths.get(logo);
		imageRun.addPicture(Files.newInputStream(imagePath), XWPFDocument.PICTURE_TYPE_PNG,
				imagePath.getFileName().toString(), Units.toEMU(50), Units.toEMU(50));

		final XWPFParagraph para1 = document.createParagraph();
		para1.setAlignment(ParagraphAlignment.BOTH);
		final String string1 = convertTextFileToString(paragraph1);
		final XWPFRun para1Run = para1.createRun();
		para1Run.setText(string1);

		final XWPFParagraph para2 = document.createParagraph();
		para2.setAlignment(ParagraphAlignment.RIGHT);
		final String string2 = convertTextFileToString(paragraph2);
		final XWPFRun para2Run = para2.createRun();
		para2Run.setText(string2);
		para2Run.setItalic(true);

		final XWPFParagraph para3 = document.createParagraph();
		para3.setAlignment(ParagraphAlignment.LEFT);
		final String string3 = convertTextFileToString(paragraph3);
		final XWPFRun para3Run = para3.createRun();
		para3Run.setText(string3);

		final FileOutputStream out = new FileOutputStream(output);
		document.write(out);
		System.err.println(document);
		out.close();
		document.close();

	}

}
