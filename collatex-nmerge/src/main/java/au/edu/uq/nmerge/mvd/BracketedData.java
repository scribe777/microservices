/*
 * NMerge is Copyright 2009-2011 Desmond Schmidt
 *
 * This file is part of NMerge. NMerge is a Java library for merging
 * multiple versions into multi-version documents (MVDs), and for
 * reading, searching and comparing them.
 *
 * NMerge is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package au.edu.uq.nmerge.mvd;

import java.io.ByteArrayOutputStream;

/**
 * Represent an object output by the MvdTool as data
 * enclosed in [..] and using a ':' to separate the
 * header (after the '[') from the data parts
 *
 * @author Desmond Schmidt 2/6/09
 */
public abstract class BracketedData {
  /**
   * the data of the chunk
   */
  protected byte[] realData;
  /**
   * the escaped data of the chunk
   */
  protected byte[] escapedData;
  /**
   * length of data parsed to produce this object
   */
  protected int srcLen;

  /**
   * Create a BracketedData object
   */
  public BracketedData() {
  }

  /**
   * Create a BracketedData object using the data
   *
   * @param data     the original data
   */
  public BracketedData(byte[] data) {
    this.realData = data;
    this.escapedData = escapeData(data);
  }

  /**
   * Ensure that any ']'s in the data are escaped so we can use them
   * to terminate the chunk when parsing it
   *
   * @param bytes the array to be escaped
   * @return the same array of bytes but ']' replaced with '\]' and
   *         '\' replaced by '\\'
   */
  protected byte[] escapeData(byte[] bytes) {
    ByteArrayOutputStream bos = new ByteArrayOutputStream(
            bytes.length + 5);
    for (int i = 0; i < bytes.length; i++) {
      if (bytes[i] == '\\') {
        bos.write('\\');
        bos.write('\\');
      } else if (bytes[i] == ']') {
        bos.write('\\');
        bos.write(']');
      } else
        bos.write(bytes[i]);
    }
    return bos.toByteArray();
  }

  /**
   * Add some data to the chunk.
   *
   * @param bytes the new bytes to add to data
   */
  public void addData(byte[] bytes) {
    if (bytes != null && bytes.length > 0) {
      byte[] newData = new byte[realData.length + bytes.length];
      for (int i = 0; i < realData.length; i++)
        newData[i] = realData[i];
      for (int j = realData.length, i = 0; i < bytes.length; i++, j++)
        newData[j] = bytes[i];
      realData = newData;
      escapedData = escapeData(realData);
    }
  }

  /**
   * Create the header which is assumed to be convertable
   * into a string
   *
   * @return the header as a String
   */
  protected abstract String createHeader();

  /**
   * Write out the chunk without converting its bytes to characters
   *
   * @return a byte array
   */
  public byte[] getBytes() {
    String header = createHeader();
    byte[] headerBytes = header.getBytes();
    byte[] totalBytes = new byte[headerBytes.length + escapedData.length + 1];
    int j = 0;
    for (int i = 0; i < headerBytes.length; i++)
      totalBytes[j++] = headerBytes[i];
    for (int i = 0; i < escapedData.length; i++)
      totalBytes[j++] = escapedData[i];
    totalBytes[j] = ']';
    return totalBytes;
  }

  /**
   * Get the length of the source data
   *
   * @return the number of bytes parsed to produce this variant
   */
  public int getSrcLen() {
    return srcLen;
  }

  /**
   * Get the read unescaped data
   *
   * @return a byte array of raw data
   */
  public byte[] getData() {
    return realData;
  }
}