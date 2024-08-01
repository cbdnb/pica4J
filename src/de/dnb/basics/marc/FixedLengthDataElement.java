/**
 *
 */
package de.dnb.basics.marc;

import java.util.Date;

import de.dnb.basics.applicationComponents.strings.StringUtils;
import de.dnb.basics.utils.TimeUtils;

/**
 * Das Feld 008.
 *
 * @author baumann
 *
 */
public class FixedLengthDataElement {

  final String fixed;

  final String dateEnteredOnFile;
  /**
   * Art des Datensatzes.
   */
  final char kindOfRecord;

  /**
   * Nicht induvidualisierter Personenname.
   */
  final char undifferentiatedPersonalName;

  public FixedLengthDataElement(final String fixed) {
    dateEnteredOnFile = StringUtils.substring(fixed, 0, 6);
    kindOfRecord = StringUtils.charAt(fixed, 9);
    undifferentiatedPersonalName = StringUtils.charAt(fixed, 32);
    this.fixed = fixed;
  }

  public RecordType getArtDesDatensatzes() {
    return RecordType.getType(kindOfRecord);
  }

  /**
   * Position 9 im Feld 008 (nur für Normdaten).
   *
   * @return default/Hinweissatz
   */
  public String getArtDesNormdatensatzes() {
    String s = "?";
    switch (kindOfRecord) {
    case 'a':
      s = "default";
      break;
    case 'b':
      s = "Hinweissatz";
      break;

    default:
      break;
    }
    return s;
  }

  /**
   *
   * @return  Datum in der Form tt.mm.jjjj
   */
  public String getEingabeDatum() {
    return TimeUtils.toDDMMYYYY(getDateEntered());
  }

  /**
   * @return  Eingabedatum
   */
  public Date getDateEntered() {
    return TimeUtils.parseMARC(dateEnteredOnFile);
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "FixedLengthDataElement [fixed=" + fixed + ", dateEnteredOnFile=" + dateEnteredOnFile
      + ", kindOfRecord=" + kindOfRecord + ", undifferentiatedPersonalName="
      + undifferentiatedPersonalName + "]";
  }

  /**
   * @param args
   */
  public static void main(final String[] args) {
    final FixedLengthDataElement authority008 =
      new FixedLengthDataElement("920117n||bzznnbbbn | aan |c");
    System.out.println(authority008.getEingabeDatum());

  }

}
