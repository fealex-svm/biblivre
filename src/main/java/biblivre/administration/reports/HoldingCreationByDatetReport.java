/*******************************************************************************
 * Este arquivo é parte do Biblivre5.
 * 
 * Biblivre5 é um software livre; você pode redistribuí-lo e/ou 
 * modificá-lo dentro dos termos da Licença Pública Geral GNU como 
 * publicada pela Fundação do Software Livre (FSF); na versão 3 da 
 * Licença, ou (caso queira) qualquer versão posterior.
 * 
 * Este programa é distribuído na esperança de que possa ser  útil, 
 * mas SEM NENHUMA GARANTIA; nem mesmo a garantia implícita de
 * MERCANTIBILIDADE OU ADEQUAÇÃO PARA UM FIM PARTICULAR. Veja a
 * Licença Pública Geral GNU para maiores detalhes.
 * 
 * Você deve ter recebido uma cópia da Licença Pública Geral GNU junto
 * com este programa, Se não, veja em <http://www.gnu.org/licenses/>.
 * 
 * @author Alberto Wagner <alberto@biblivre.org.br>
 * @author Danniel Willian <danniel@biblivre.org.br>
 ******************************************************************************/
package biblivre.administration.reports;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.pdf.PdfPTable;

import biblivre.administration.reports.dto.HoldingCreationByDateReportDto;
import biblivre.core.utils.CharPool;

public class HoldingCreationByDatetReport extends BaseBiblivreReport<HoldingCreationByDateReportDto> {

	private static Map<String, Integer> userTotal;

	@Override
	protected HoldingCreationByDateReportDto getReportData(ReportsDTO dto) {
		ReportsDAO dao = ReportsDAO.getInstance(this.getSchema());
		String initialDate = format(dto.getInitialDate());
		String finalDate = format(dto.getFinalDate());
		return dao.getHoldingCreationByDateReportData(initialDate, finalDate);
	}

	@Override
	protected void generateReportBody(Document document, HoldingCreationByDateReportDto reportData) throws Exception {
		userTotal = new HashMap<String, Integer>();

		String dateSpan =
			new StringBuilder(7)
				.append(getText("administration.reports.field.date_from"))
				.append(CharPool.SPACE)
				.append(reportData.getInitialDate())
				.append(CharPool.SPACE)
				.append(getText("administration.reports.field.date_to"))
				.append(CharPool.SPACE)
				.append(reportData.getFinalDate())
				.toString();

		ReportUtil.insertChunkedTextParagraph(
				document, ReportUtil::getHeaderChunk, Element.ALIGN_LEFT, dateSpan);

		ReportUtil.insertNewLine(document);

		if (reportData.getData() != null) {
			PdfPTable table = createTable(reportData.getData());

			document.add(table);

			ReportUtil.insertNewLine(document);
		}

		if (userTotal.size() > 0) {
			_insertUserTotalsTable(document);

			ReportUtil.insertNewLine(document);
		}

		//Database totals table
		_insertTotalsTitle(document);

		ReportUtil.insertNewLine(document);

		_insertTotalsTable(document, reportData);

	}

	private void _insertUserTotalsTable(Document document) throws DocumentException {
		PdfPTable table = new PdfPTable(2);

		ReportUtil.insertChunkedTextCellWithStrategy(
				table, ReportUtil::getHeaderChunk, ReportUtil.CENTER,
				getText("administration.reports.title.user_creation_count"));

		ReportUtil.insertNewLine(document);

		ReportUtil.insertChunkedTextCellWithStrategy(
				table, ReportUtil::getHeaderChunk, ReportUtil.BORDER_BACKGROUND_LEFT_MIDDLE,
				getText("administration.reports.field.user_name"));

		ReportUtil.insertChunkedTextCellWithStrategy(
				table, ReportUtil::getHeaderChunk, ReportUtil.BORDER_BACKGROUND_CENTER_MIDDLE,
				getText("administration.reports.field.total"));

		userTotal.forEach((userName, total) -> {
			ReportUtil.insertChunkedTextCellWithStrategy(
					table, ReportUtil::getNormalChunk, ReportUtil.LEFT_MIDDLE, userName);

			ReportUtil.insertChunkedTextCellWithStrategy(
					table, ReportUtil::getNormalChunk, ReportUtil.CENTER_MIDDLE,
					String.valueOf(total));
		});

		document.add(table);
	}

	public void _insertTotalsTitle(Document document) throws DocumentException {
		ReportUtil.insertChunkedTextParagraph(
				document, ReportUtil::getHeaderChunk, Element.ALIGN_CENTER,
				getText("administration.reports.field.database_count"));
	}

	private void _insertTotalsTable(Document document, HoldingCreationByDateReportDto reportData)
			throws DocumentException {
		PdfPTable table = new PdfPTable(3);

		ReportUtil.insertChunkedTextCellWithStrategy(
				table, ReportUtil::getHeaderChunk, ReportUtil.LEFT,
				getText("administration.reports.field.database_count"));

		ReportUtil.insertChunkedTextCellWithStrategy(
				table, ReportUtil::getHeaderChunk, ReportUtil.CENTER,
				getText("administration.reports.field.database_count"));

		ReportUtil.insertChunkedTextCellWithStrategy(
				table, ReportUtil::getHeaderChunk, ReportUtil.CENTER,
				getText("administration.reports.field.database_count"));

		ReportUtil.insertChunkedTextCellWithStrategy(
				table, ReportUtil::getHeaderChunk, ReportUtil.BACKGROUND_LEFT_MIDDLE,
				getText("administration.reports.field.database_main"));

		String totalBiblioMain = StringUtils.defaultString(reportData.getTotalBiblioMain());

		ReportUtil.insertChunkedTextCellWithStrategy(
				table, ReportUtil::getNormalChunk, ReportUtil.CENTER_MIDDLE, totalBiblioMain);

		String totalHoldingMain = StringUtils.defaultString(reportData.getTotalHoldingMain());

		ReportUtil.insertChunkedTextCellWithStrategy(
				table, ReportUtil::getNormalChunk, ReportUtil.CENTER_MIDDLE, totalHoldingMain);

		ReportUtil.insertChunkedTextCellWithStrategy(
				table, ReportUtil::getHeaderChunk, ReportUtil.BACKGROUND_LEFT_MIDDLE,
				getText("administration.reports.field.database_work"));

		String totalBiblioWork = StringUtils.defaultString(reportData.getTotalBiblioWork());

		ReportUtil.insertChunkedTextCellWithStrategy(
				table, ReportUtil::getNormalChunk, ReportUtil.CENTER_MIDDLE, totalBiblioWork);

		String totalHoldingWork = StringUtils.defaultString(reportData.getTotalHoldingWork());

		ReportUtil.insertChunkedTextCellWithStrategy(
				table, ReportUtil::getNormalChunk, ReportUtil.CENTER_MIDDLE, totalHoldingWork);

		document.add(table);
	}

	private PdfPTable createTable(List<String[]> dataList) {
		PdfPTable table = new PdfPTable(4);

		table.setHorizontalAlignment(Element.ALIGN_CENTER);

		createHeader(table);

		for (String[] data : dataList) {
			String name = data[1];

			int total = Integer.valueOf(data[2]);

			if (userTotal.containsKey(name)) {
				userTotal.put(name, userTotal.get(name) + total);
			} else {
				userTotal.put(name, total);
			}

			ReportUtil.insertChunkedTextCellWithStrategy(
					table, ReportUtil::getNormalChunk, ReportUtil.LEFT_MIDDLE, data[0]);

			ReportUtil.insertChunkedTextCellWithStrategy(
					table, ReportUtil::getNormalChunk,
					ReportUtil.LEFT_MIDDLE.with(ReportUtil.COLSPAN.apply(2)), name);

			ReportUtil.insertChunkedTextCellWithStrategy(
					table, ReportUtil::getNormalChunk, ReportUtil.CENTER_MIDDLE, data[2]);
		}

		return table;
	}

	private void createHeader(PdfPTable table) {
		ReportUtil.insertChunkedTextCellWithStrategy(
				table, ReportUtil::getHeaderChunk, ReportUtil.BORDER_BACKGROUND_LEFT_MIDDLE,
				getText("administration.reports.field.date"));

		ReportUtil.insertChunkedTextCellWithStrategy(
				table, ReportUtil::getHeaderChunk,
				ReportUtil.BORDER_BACKGROUND_LEFT_MIDDLE.with(ReportUtil.COLSPAN.apply(2)),
				getText("administration.reports.field.user_name"));

		ReportUtil.insertChunkedTextCellWithStrategy(
				table, ReportUtil::getHeaderChunk, ReportUtil.BORDER_BACKGROUND_LEFT_MIDDLE,
				getText("administration.reports.field.total"));
	}

	@Override
	protected String getTitle() {
		return this.getText("administration.reports.title.holdings_creation_by_date");
	}

}
