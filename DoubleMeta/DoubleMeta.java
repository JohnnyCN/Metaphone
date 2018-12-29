/*
Jazzy - a Java library for Spell Checking
Copyright (C) 2001 Mindaugas Idzelis
Full text of license can be found in LICENSE.txt

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
*/
package com.swabunga.spell.engine;

/**
 * A phonetic encoding algorithm that takes an English word and computes a
 * phonetic version of it. This allows for phonetic matches in a spell checker.
 * This class is a port of the C++ DoubleMetaphone() class, which was intended
 * to return two possible phonetic translations for certain words, although the
 * Java version only seems to be concerned with one, making the "double" part
 * erroneous. <br>
 * source code for the original C++ can be found here: <a href=
 * "http://aspell.sourceforge.net/metaphone/"/>http://aspell.sourceforge.net/metaphone/</a>
 * DoubleMetaphone does some processing, such as uppercasing, on the input
 * string first to normalize it. Then, to create the key, the function traverses
 * the input string in a while loop, sending successive characters into a giant
 * switch statement. Before determining the appropriate pronunciation, the
 * algorithm considers the context surrounding each character within the input
 * string.
 * <p>
 * Things that were changed: <br/>
 * The alternate flag could be set to true but was never checked so why bother
 * with it. REMOVED <br/>
 * Why was this class serializable? <br/>
 * The primary, in, length and last variables could be initialized and local to
 * the process method and references passed around the appropriate methods. As
 * such there are no class variables and this class becomes firstly threadsafe
 * and secondly could be static final. <br/>
 * The function call SlavoGermaic was called repeatedly in the process function,
 * it is now only called once.
 *
 */
public class DoubleMeta implements Transformator {

	/**
	 * The replace list is used in the getSuggestions method. All of the letters in
	 * the misspelled word are replaced with the characters from this list to try
	 * and generate more suggestions, which implies l*n tries, if l is the size of
	 * the string, and n is the size of this list.
	 *
	 * In addition to that, each of these letters is added to the misspelled word.
	 */
	/*
	 * Metaphone �㷨ʹ���� 16 �������࣬�������ַ�����
	 * B X S K J T F H L M N P R 0 W Y �ַ� 0 ���㣬�������� th ��������
	 * ������ Soundex �㷨��һ������һ����ĸ�����������Ĵ��뱻�ü����ĸ��ַ���������������ĸ��ַ���Ҳ������䡣
	 * �ظ�����ĸ��Ԫ��ͨ����ɾ������Ԫ���Ĵ���һ����Metaphone �㷨��������һ�׹��򼯣����԰���ĸ���ӳ��ɸ����ࡣ
	 * 
	 * DoubleMetaphone �㷨��ԭ���ĸ���������һЩ����,�������еĿ�ʼԪ��������� A�����Բ���ʹ�� Soundex �㷨��
	 * ���Ӹ����ı仯�ǣ�DoubleMetaphone ����д�ɿ���Ϊ�����ʷ��ز�ͬ�Ĵ��롣
	 * ���磬 hegemony�е� g ���Է�������Ҳ���Է������������㷨�ȷ��� HJMN ��Ҳ���Է��� HKMN ��
	 * ������Щ����֮�⣬Metaphone �㷨�еĶ������ʻ��Ƿ��ص�һ����
	 * list26\list49\list96����
	 */
	private static char[] replaceList = { 'A', 'B', 'X', 'S', 'K', 'J', 'T', 'F', 'H', 'L', 'M', 'N', 'P', 'R', '0' };

	private static final String[] myList = { "GN", "KN", "PN", "WR", "PS", "" };
	private static final String[] list1 = { "ACH", "" };
	private static final String[] list2 = { "BACHER", "MACHER", "" };
	private static final String[] list3 = { "CAESAR", "" };
	private static final String[] list4 = { "CHIA", "" };
	private static final String[] list5 = { "CH", "" };
	private static final String[] list6 = { "CHAE", "" };
	private static final String[] list7 = { "HARAC", "HARIS", "" };
	private static final String[] list8 = { "HOR", "HYM", "HIA", "HEM", "" };
	private static final String[] list9 = { "CHORE", "" };
	private static final String[] list10 = { "VAN ", "VON ", "" };
	private static final String[] list11 = { "SCH", "" };
	private static final String[] list12 = { "ORCHES", "ARCHIT", "ORCHID", "" };
	private static final String[] list13 = { "T", "S", "" };
	private static final String[] list14 = { "A", "O", "U", "E", "" };
	private static final String[] list15 = { "L", "R", "N", "M", "B", "H", "F", "V", "W", " ", "" };
	private static final String[] list16 = { "MC", "" };
	private static final String[] list17 = { "CZ", "" };
	private static final String[] list18 = { "WICZ", "" };
	private static final String[] list19 = { "CIA", "" };
	private static final String[] list20 = { "CC", "" };
	private static final String[] list21 = { "I", "E", "H", "" };
	private static final String[] list22 = { "HU", "" };
	private static final String[] list23 = { "UCCEE", "UCCES", "" };
	private static final String[] list24 = { "CK", "CG", "CQ", "" };
	private static final String[] list25 = { "CI", "CE", "CY", "" };
	// DMV: used by the original code which returned two phonetic code, but not the
	// current code
	// private static final String[] list26 = {
	// "CIO", "CIE", "CIA", ""
	// };
	private static final String[] list27 = { " C", " Q", " G", "" };
	private static final String[] list28 = { "C", "K", "Q", "" };
	private static final String[] list29 = { "CE", "CI", "" };
	private static final String[] list30 = { "DG", "" };
	private static final String[] list31 = { "I", "E", "Y", "" };
	private static final String[] list32 = { "DT", "DD", "" };
	private static final String[] list33 = { "B", "H", "D", "" };
	private static final String[] list34 = { "B", "H", "D", "" };
	private static final String[] list35 = { "B", "H", "" };
	private static final String[] list36 = { "C", "G", "L", "R", "T", "" };
	private static final String[] list37 = { "EY", "" };
	private static final String[] list38 = { "LI", "" };
	private static final String[] list39 = { "ES", "EP", "EB", "EL", "EY", "IB", "IL", "IN", "IE", "EI", "ER", "" };
	private static final String[] list40 = { "ER", "" };
	private static final String[] list41 = { "DANGER", "RANGER", "MANGER", "" };
	private static final String[] list42 = { "E", "I", "" };
	private static final String[] list43 = { "RGY", "OGY", "" };
	private static final String[] list44 = { "E", "I", "Y", "" };
	private static final String[] list45 = { "AGGI", "OGGI", "" };
	private static final String[] list46 = { "VAN ", "VON ", "" };
	private static final String[] list47 = { "SCH", "" };
	private static final String[] list48 = { "ET", "" };

	// DMV: used by the orininal code which returned two phonetic code, but not the
	// current code
	// private static final String[] list49 = {
	// "IER ", ""
	// };
	private static final String[] list50 = { "JOSE", "" };
	private static final String[] list51 = { "SAN ", "" };
	private static final String[] list52 = { "SAN ", "" };
	private static final String[] list53 = { "JOSE", "" };
	private static final String[] list54 = { "L", "T", "K", "S", "N", "M", "B", "Z", "" };
	private static final String[] list55 = { "S", "K", "L", "" };
	private static final String[] list56 = { "ILLO", "ILLA", "ALLE", "" };
	private static final String[] list57 = { "AS", "OS", "" };
	private static final String[] list58 = { "A", "O", "" };
	private static final String[] list59 = { "ALLE", "" };
	private static final String[] list60 = { "UMB", "" };
	private static final String[] list61 = { "ER", "" };
	private static final String[] list62 = { "P", "B", "" };
	private static final String[] list63 = { "IE", "" };
	private static final String[] list64 = { "ME", "MA", "" };
	private static final String[] list65 = { "ISL", "YSL", "" };
	private static final String[] list66 = { "SUGAR", "" };
	private static final String[] list67 = { "SH", "" };
	private static final String[] list68 = { "HEIM", "HOEK", "HOLM", "HOLZ", "" };
	private static final String[] list69 = { "SIO", "SIA", "" };
	private static final String[] list70 = { "SIAN", "" };
	private static final String[] list71 = { "M", "N", "L", "W", "" };
	private static final String[] list72 = { "Z", "" };
	private static final String[] list73 = { "Z", "" };
	private static final String[] list74 = { "SC", "" };
	private static final String[] list75 = { "OO", "ER", "EN", "UY", "ED", "EM", "" };
	private static final String[] list76 = { "ER", "EN", "" };
	private static final String[] list77 = { "I", "E", "Y", "" };
	private static final String[] list78 = { "AI", "OI", "" };
	private static final String[] list79 = { "S", "Z", "" };
	private static final String[] list80 = { "TION", "" };
	private static final String[] list81 = { "TIA", "TCH", "" };
	private static final String[] list82 = { "TH", "" };
	private static final String[] list83 = { "TTH", "" };
	private static final String[] list84 = { "OM", "AM", "" };
	private static final String[] list85 = { "VAN ", "VON ", "" };
	private static final String[] list86 = { "SCH", "" };
	private static final String[] list87 = { "T", "D", "" };
	private static final String[] list88 = { "WR", "" };
	private static final String[] list89 = { "WH", "" };
	private static final String[] list90 = { "EWSKI", "EWSKY", "OWSKI", "OWSKY", "" };
	private static final String[] list91 = { "SCH", "" };
	private static final String[] list92 = { "WICZ", "WITZ", "" };
	private static final String[] list93 = { "IAU", "EAU", "" };
	private static final String[] list94 = { "AU", "OU", "" };
	private static final String[] list95 = { "C", "X", "" };

	// DMV: used by the orininal code which returned two phonetic code, but not the
	// current code
	// private static final String[] list96 = {
	// "ZO", "ZI", "ZA", ""
	// };

	/**
	 * ˹�����ն������б�
	 * 
	 * @param in ����
	 * 
	 * @return �Ƿ����˹�����ն������ַ�
	 */
	private final static boolean SlavoGermanic(String in) {
		// �������������һ�ַ��򷵻�true
		if ((in.indexOf("W") > -1) || (in.indexOf("K") > -1) || (in.indexOf("CZ") > -1) || (in.indexOf("WITZ") > -1))
			return true;
		return false;
	}

	/**
	 * ��Ӹ�����ĸ����������
	 * 
	 * @param primary ��������
	 * @param main Ҫ��ӵĸ�����ĸ
	 */
	private final static void MetaphAdd(StringBuffer primary, String main) {
		if (main != null) {
			primary.append(main);
		}
	}

	/**
	 * ��Ӹ�����ĸ����������
	 *  
	 * @param primary ��������
	 * @param main Ҫ��ӵĸ�����ĸ
	 */
	private final static void MetaphAdd(StringBuffer primary, char main) {
		primary.append(main);
	}

	/**
	 * �ж��Ƿ�ΪԪ����ĸ
	 * 
	 * @param in �ַ��������ʣ�
	 * @param at ��ǰ�ַ����ַ����е�λ��
	 * @param length �ַ�������
	 * 
	 * @return �Ƿ�ΪԪ����ĸ
	 */
	private final static boolean isVowel(String in, int at, int length) {
		if ((at < 0) || (at >= length))
			return false;
		char it = in.charAt(at);
		if ((it == 'A') || (it == 'E') || (it == 'I') || (it == 'O') || (it == 'U') || (it == 'Y'))
			return true;
		return false;
	}

	/**
	 * ����ƥ�䵥���е��ַ���
	 * 
	 * @param string �����ַ���
	 * @param start ��ʼλ��
	 * @param length Ҫ��ȡ���ַ�������
	 * @param list Ҫƥ����ַ����б�����ƥ���ȡ���ַ�����
	 * 
	 * @return �����Ƿ�ƥ��
	 */
	private final static boolean stringAt(String string, int start, int length, String[] list) {
		if ((start < 0) || (start >= string.length()) || list.length == 0)
			return false;
		//��ȡ�����ַ��� string ���ִ�
		String substr = string.substring(start, start + length);
		for (int i = 0; i < list.length; i++) {
			if (list[i].equals(substr))
				return true;
		}
		return false;
	}

	/**
	 * Take the given word, and return the best phonetic hash for it. Vowels are
	 * minimized as much as possible, and consonants that have similiar sounds are
	 * converted to the same consonant for example, 'v' and 'f' are both converted
	 * to 'f'
	 * 
	 * @param word the text to transform
	 * @return the result of the phonetic transformation
	 */
	// ���뵥�ʣ���������ʵ�����ɢ��
	/*
	 * 1. ����Ӣ�ﵥ�ʡ�
	 * 2. ɾ������Ա����뵥���еķ�Ӣ����ĸ�ַ�������ĸȫ��ת��Ϊ��д��ʽ��
	 * 3. �Դ��׵���ĸ����ĸ��Ͻ��б���ǰ��Ԥ����
	 * ��ĸ��� AE-��GN-��KN-��PN- �� WR- λ�ڴ���ʱ��ɾ�����е�����ĸ��
	 * ɾ��λ�ڴ��׵���ĸ��� WH- �е� H����λ�ڴ��׵���ĸ X �滻�� S ��
	 * 4. �����ڵ��ظ���ĸ�������ش���
	 * 5. ���ձ��������б��루�����������룩��
	 * (1) Ԫ����ĸ A��E��I��O��U λ�ڴ���ʱ���Ա�����λ������λ��ʱ��ɾ����
	 * (2) �Ա����뵥�ʵĳ�����ĸ֮������и�����ĸ���� Metaphone ����������ת�������й涨�Ķ�Ӧ��ϵ����ת����ɾ������
	 * 6. ��� Metaphone �������롣
	 */
	public final String transform(String word) {
		// ��ʼ���������볤��Ϊ���ʳ���+5
		StringBuffer primary = new StringBuffer(word.length() + 5);
		// ����ĸȫ��ת��Ϊ��д��ʽ������5���ո�
		String in = word.toUpperCase() + "     ";
		// �趨���ַ����е�λ�ã�0Ϊ���ף�
		int current = 0;
		// �趨�ַ����ĳ���
		int length = in.length();
		// ����ַ�������С��1�򷵻ؿ�
		if (length < 1)
			return "";
		// �趨����λ��
		int last = length - 1;
		// �ж��Ƿ���˹�����ն������ַ�
		boolean isSlavoGermaic = SlavoGermanic(in);
		// ��ĸ���AE-��GN-��KN-��PN-��WR-����myListΪ׼��λ�ڴ���ʱ��
		// ɾ�����е�����ĸ��λ��ǰ��һλ�����������������У�
		// ���ǰ��λƥ��myList
		if (stringAt(in, 0, 2, myList))
			current += 1;
		// ��λ�ڴ��׵���ĸX�滻��S���浽����������
		if (in.charAt(0) == 'X') {
			MetaphAdd(primary, 'S');
			current += 1;
		}
		// Ԫ����ĸ A��E��I��O��U λ�ڴ���ʱ���Ա�����λ������λ��ʱ��ɾ��
		while (current < length) {
			// ��ȡ��ǰλ�õ��ַ�����ǰλ��Ϊ3��2��1������ΪA E I O Uʱ�����current = 0��
			switch (in.charAt(current)) {
			case 'A':
			case 'E':
			case 'I':
			case 'O':
			case 'U':
			case 'Y':
				// �Ѵ��׵�Y��ΪA�ӵ�����������
				if (current == 0)
					MetaphAdd(primary, 'A');
				current += 1;
				break;
			case 'B':
				MetaphAdd(primary, 'P');
				// �����һλҲ��B��ֻ���һ��P������������
				if (in.charAt(current + 1) == 'B')
					current += 2;
				else
					current += 1;
				break;
			// ���б������ŵ�������ĸ��дC��C/C++/Java source code	"\u00C7"��
			case '\u00C7':
				// ��ΪS�ӵ�����������
				MetaphAdd(primary, 'S');
				current += 1;
				break;
			case 'C':
				// ������ڵ���ǰ��λ����ǰ��λ����Ԫ���Ҵ�ǰһλ��ʼ3����ĸƥ��list1��������λ����I��E
				// ������ǰ��2λ��ʼ6����ĸƥ��list2
				if ((current > 1) && !isVowel(in, current - 2, length) && stringAt(in, (current - 1), 3, list1)
						&& (in.charAt(current + 2) != 'I') && (in.charAt(current + 2) != 'E')
						|| stringAt(in, (current - 2), 6, list2)) {
					// ��ΪK���뵽����������
					MetaphAdd(primary, 'K');
					// ���Ժ�һλ
					current += 2;
					break;
				}
				// ���λ�ڴ����Ҵӵ�ǰ��ĸ��ʼ6����ĸƥ��list3
				if ((current == 0) && stringAt(in, current, 6, list3)) {
					// ��ΪS���뵽����������
					MetaphAdd(primary, 'S');
					// ���Ժ�һλ
					current += 2;
					break;
				}
				// ����ӵ�ǰ��ĸ��ʼ4����ĸƥ��list4
				if (stringAt(in, current, 4, list4)) {
					// ���ΪK���뵽����������
					MetaphAdd(primary, 'K');
					// ���Ժ�һλ
					current += 2;
					break;
				}
				// ����ӵ�ǰ��ĸ��ʼ2����ĸƥ��list5
				if (stringAt(in, current, 2, list5)) {
					// ������Ǵ����Ҵӵ�ǰ��ĸ��ʼ4����ĸƥ��list6
					if ((current > 0) && stringAt(in, current, 4, list6)) {
						// ��ΪK���뵽����������
						MetaphAdd(primary, 'K');
						// ���Ժ�һλ
						current += 2;
						break;
					}
					// ���λ�ڴ����ҴӺ�һ����ĸ��ʼ5����ĸƥ��list7
					// ��Ӻ�һ����ĸ��ʼ3����ĸƥ��list8�ҵ��ʵ�ǰ5����ĸ��ƥ��list9
					if ((current == 0) && stringAt(in, (current + 1), 5, list7)
							|| stringAt(in, current + 1, 3, list8) && !stringAt(in, 0, 5, list9)) {
						// ��ΪK���뵽����������
						MetaphAdd(primary, 'K');
						// ���Ժ�һλ
						current += 2;
						break;
					}
					// ������ʵ�ǰ4����ĸƥ��list10�򵥴ʵ�ǰ������ĸƥ��list11����ǰ��2����ĸ��ʼ6����ĸƥ��list12
					// ���������2����ĸƥ��list13����(ǰ�����ĸƥ��list14��ǰλ�ڴ���)���������2����ĸƥ��list15
					if (stringAt(in, 0, 4, list10) || stringAt(in, 0, 3, list11) || stringAt(in, current - 2, 6, list12)
							|| stringAt(in, current + 2, 1, list13)
							|| (stringAt(in, current - 1, 1, list14) || (current == 0))
									&& stringAt(in, current + 2, 1, list15)) {
						// ��ΪK���뵽����������
						MetaphAdd(primary, 'K');
					} else {
						// ������ڴ���
						if (current > 0) {
							// �������ǰ������ĸƥ��list16
							if (stringAt(in, 0, 2, list16))
								// ��ΪK���뵽����������
								MetaphAdd(primary, 'K');
							else
								// ��ΪX���뵽����������
								MetaphAdd(primary, 'X');
						} else {
							// ��ΪX���뵽����������
							MetaphAdd(primary, 'X');
						}
					}
					// ���Ժ�һλ
					current += 2;
					break;
				}
				// ����ӵ�ǰ��ʼ2����ĸƥ��list17�Ҵӵ�ǰ��ʼ4����ĸ��ƥ��list18
				if (stringAt(in, current, 2, list17) && !stringAt(in, current, 4, list18)) {
					// ��ΪS���뵽����������
					MetaphAdd(primary, 'S');
					// ���Ժ�һλ
					current += 2;
					break;
				}
				// ����ӵ�ǰ��ʼ2����ĸƥ��list19
				if (stringAt(in, current, 2, list19)) {
					// ��ΪX���뵽����������
					MetaphAdd(primary, 'X');
					// ���Ժ�һλ
					current += 2;
					break;
				}
				// ����ӵ�ǰ��ʼ2����ĸƥ��list20�Ҳ��ǣ��ڵ��ʵ�2����ĸ��λ���ҵ�������ĸΪM��MC��
				if (stringAt(in, current, 2, list20) && !((current == 1) && in.charAt(0) == 'M')) {
					// ��������2����ĸƥ��list21�������2����ĸ��ʼ2����ĸ��ƥ��list22
					if (stringAt(in, current + 2, 1, list21) && !stringAt(in, current + 2, 2, list22)) {
						// �������ǰλ��Ϊ2����һ��λ��ΪA��AC�� �� ��ǰһ����ĸ��ʼ5����ĸƥ��list23
						if (((current == 1) && (in.charAt(current - 1) == 'A'))
								|| stringAt(in, (current - 1), 5, list23))
							// ��ΪKS���뵽����������
							MetaphAdd(primary, "KS");
						else
							// ��ΪX���뵽����������
							MetaphAdd(primary, 'X');
						// ���Ժ���λ
						current += 3;
						break;
					} else {
						// ��ΪK���뵽����������
						MetaphAdd(primary, 'K');
						// ���Ժ�һλ
						current += 2;
						break;
					}
				}
				// ����ӵ�ǰ��ĸ��ʼ2����ĸƥ��list24
				if (stringAt(in, current, 2, list24)) {
					// ��ΪK���뵽����������
					MetaphAdd(primary, 'K');
					// ���Ժ�һλ
					current += 2;
					break;
				// ����ӵ�ǰ��ĸ��ʼ2����ĸƥ��list25
				} else if (stringAt(in, current, 2, list25)) {
					// ��ΪS���뵽����������
					MetaphAdd(primary, 'S');
					// ���Ժ�һλ
					current += 2;
					break;
				}
				// ��ΪK���뵽����������
				MetaphAdd(primary, 'K');
				// ����Ӻ�һ����ĸ��ʼ2����ĸƥ��list27
				if (stringAt(in, current + 1, 2, list27))
					// ���Ժ���λ
					current += 3;
				// �����һ����ĸƥ��list28�Һ�һ����ĸ��ʼ2����ĸ��ƥ��list29
				else if (stringAt(in, current + 1, 1, list28) && !stringAt(in, current + 1, 2, list29))
					// ���Ժ�һλ
					current += 2;
				else
					current += 1;
				break;
			case 'D':
				// ����ӵ�ǰ��ĸ��ʼ2����ĸƥ��list30
				if (stringAt(in, current, 2, list30)) {
					// ��������2����ĸƥ��list31
					if (stringAt(in, current + 2, 1, list31)) {
						// ��ΪJ���뵽����������
						MetaphAdd(primary, 'J');
						// ���Ժ���λ
						current += 3;
						break;
					} else {
						// ��ΪTK���뵽����������
						MetaphAdd(primary, "TK");
						// ���Ժ�һλ
						current += 2;
						break;
					}
				}
				// ��ΪT���뵽����������
				MetaphAdd(primary, 'T');
				// ����ӵ�ǰ��ĸ��ʼ2����ĸƥ��list32
				if (stringAt(in, current, 2, list32)) {
					// ���Ժ�һλ
					current += 2;
				} else {
					current += 1;
				}
				break;
			case 'F':
				// �����һλҲ��F
				if (in.charAt(current + 1) == 'F')
					// ���Ժ�һλ
					current += 2;
				else
					current += 1;
				// ��ΪF���뵽����������
				MetaphAdd(primary, 'F');
				break;
			case 'G':
				// �����һλ��H
				if (in.charAt(current + 1) == 'H') {
					// ������Ǵ�����ǰһ����ĸ����Ԫ��
					if ((current > 0) && !isVowel(in, current - 1, length)) {
						// ��ΪK���뵽����������
						MetaphAdd(primary, 'K');
						// ���Ժ�һλ
						current += 2;
						break;
					}
					// �����ǰλ���ڵ���ǰ��λ
					if (current < 3) {
						// ���λ�ڴ���
						if (current == 0) {
							// �������ڶ�λΪI
							if (in.charAt(current + 2) == 'I')
								// ��ΪJ���뵽����������
								MetaphAdd(primary, 'J');
							else
								// ��ΪK���뵽����������
								MetaphAdd(primary, 'K');
							// ���Ժ�һλ
							current += 2;
							break;
						}
					}
					// ������ڵ���ǰ2λ����ǰ��2λƥ��list33
					// ���ڵ���ǰ3λ����ǰ��3λƥ��list34
					// ���ڵ���ǰ4λ����ǰ��4λƥ��list35
					if ((current > 1) && stringAt(in, current - 2, 1, list33)
							|| ((current > 2) && stringAt(in, current - 3, 1, list34))
							|| ((current > 3) && stringAt(in, current - 4, 1, list35))) {
						// ���Ժ�һλ
						current += 2;
						break;
					} else {
						// ������ڵ���ǰ3λ��ǰ1λ����U����ǰ��3λƥ��list36
						if ((current > 2) && (in.charAt(current - 1) == 'U') && stringAt(in, current - 3, 1, list36)) {
							// ��ΪF���뵽����������
							MetaphAdd(primary, 'F');
						} else {
							// ������ڴ�����ǰ1λ����I
							if ((current > 0) && (in.charAt(current - 1) != 'I'))
								// ��ΪK���뵽����������
								MetaphAdd(primary, 'K');
						}
						// ���Ժ�һλ
						current += 2;
						break;
					}
				}
				// �����һλ��N
				if (in.charAt(current + 1) == 'N') {
					// �����ǰ�ǵ��ʵĵڶ�����ĸ������ĸ��Ԫ���Ҳ���˹�����ն�����
					if ((current == 1) && isVowel(in, 0, length) && !isSlavoGermaic) {
						// ��ΪKN���뵽����������
						MetaphAdd(primary, "KN");
					} else {
						// �������ڶ�λ��ʼ2����ĸƥ��list37�Һ�һ����ĸ����Y�Ҳ���˹�����ն�����
						if (!stringAt(in, current + 2, 2, list37) && (in.charAt(current + 1) != 'Y')
								&& !isSlavoGermaic) {
							// ��ΪN���뵽����������
							MetaphAdd(primary, "N");
						} else {
							// ��ΪKN���뵽����������
							MetaphAdd(primary, "KN");
						}
					}
					// ���Ժ�һλ
					current += 2;
					break;
				}
				// ����Ӻ�һλ��ʼ2����ĸƥ��list38�Ҳ���˹�����ն�����
				if (stringAt(in, current + 1, 2, list38) && !isSlavoGermaic) {
					// ��ΪKL���뵽����������
					MetaphAdd(primary, "KL");
					// ���Ժ�һλ
					current += 2;
					break;
				}
				// ������ڴ����Һ�һλ����Y��Ӻ�һλ��ʼ2����ĸƥ��list39
				if ((current == 0) && ((in.charAt(current + 1) == 'Y') || stringAt(in, current + 1, 2, list39))) {
					// ��ΪK���뵽����������
					MetaphAdd(primary, 'K');
					// ���Ժ�һλ
					current += 2;
					break;
				}
				// ����Ӻ�һλ��ʼ2����ĸƥ��list40���һλ��Y
				// �ҵ���ǰ6����ĸ��ƥ��list41��ǰһ����ĸ��ƥ��list42�Ҵ�ǰһ����ĸ��ʼ3����ĸ��ƥ��list43
				if ((stringAt(in, current + 1, 2, list40) || (in.charAt(current + 1) == 'Y'))
						&& !stringAt(in, 0, 6, list41) && !stringAt(in, current - 1, 1, list42)
						&& !stringAt(in, current - 1, 3, list43)) {
					// ��ΪK���뵽����������
					MetaphAdd(primary, 'K');
					// ���Ժ�һ����ĸ
					current += 2;
					break;
				}
				// �����һ����ĸƥ��list44���ǰһ����ĸ��ʼ4����ĸƥ��list45
				if (stringAt(in, current + 1, 1, list44) || stringAt(in, current - 1, 4, list45)) {
					// �������ǰ�ĸ���ĸƥ��list46�򵥴�ǰ������ĸƥ��list47��Ӻ�һ����ĸ��ʼ2����ĸƥ��list48
					if (stringAt(in, 0, 4, list46) || stringAt(in, 0, 3, list47)
							|| stringAt(in, current + 1, 2, list48)) {
						// ��ΪK���뵽����������
						MetaphAdd(primary, 'K');
					} else {
						// ��ΪJ���뵽����������
						MetaphAdd(primary, 'J');
					}
					// ���Ժ�һλ
					current += 2;
					break;
				}
				// �����һ����ĸ��G
				if (in.charAt(current + 1) == 'G')
					// ���Ժ�һλ
					current += 2;
				else
					current += 1;
				// ��ΪK���뵽����������
				MetaphAdd(primary, 'K');
				break;
			case 'H':
				// ������ڴ��׻�ǰһ����ĸ��Ԫ�����Һ�һ����ĸ��Ԫ��
				if (((current == 0) || isVowel(in, current - 1, length)) && isVowel(in, current + 1, length)) {
					// ��ΪH���뵽����������
					MetaphAdd(primary, 'H');
					// ���Ժ�һλ
					current += 2;
				} else {
					current += 1;
				}
				break;
			case 'J':
				// ����ӵ�ǰ��ĸ��ʼ4����ĸƥ��list50�򵥴�ǰ�ĸ���ĸƥ��list51
				if (stringAt(in, current, 4, list50) || stringAt(in, 0, 4, list51)) {
					// ����ڴ�����������ĸ���ĸΪ�ջ򵥴�ǰ�ĸ���ĸƥ��list52
					if ((current == 0) && (in.charAt(current + 4) == ' ') || stringAt(in, 0, 4, list52)) {
						// ��ΪH���뵽����������
						MetaphAdd(primary, 'H');
					} else {
						// // ��ΪJ���뵽����������
						MetaphAdd(primary, 'J');
					}
					current += 1;
					break;
				}
				// ����ڴ����Ҵӵ�ǰ��ĸ��ʼ4����ĸƥ��list53
				if ((current == 0) && !stringAt(in, current, 4, list53)) {
					// ��ΪJ���뵽����������
					MetaphAdd(primary, 'J');
				} else {
					// ���ǰһ����ĸ��Ԫ���Ҳ���˹�����ն������ң���һ����ĸ��A���һ����ĸ��O��
					if (isVowel(in, current - 1, length) && !isSlavoGermaic
							&& ((in.charAt(current + 1) == 'A') || in.charAt(current + 1) == 'O')) {
						// ��ΪJ���뵽����������
						MetaphAdd(primary, 'J');
					} else {
						// ��������һ����ĸ
						if (current == last) {
							// ��ΪJ���뵽����������
							MetaphAdd(primary, 'J');
						} else {
							// �����һλƥ��list54��ǰһλƥ��list55
							if (!stringAt(in, current + 1, 1, list54) && !stringAt(in, current - 1, 1, list55)) {
								// ��ΪJ���뵽����������
								MetaphAdd(primary, 'J');
							}
						}
					}
				}
				// �����һλ��J
				if (in.charAt(current + 1) == 'J')
					// ���Ժ�һλ
					current += 2;
				else
					current += 1;
				break;
			case 'K':
				// �����һλ��K
				if (in.charAt(current + 1) == 'K')
					// ���Ժ�һλ
					current += 2;
				else
					current += 1;
				// ��ΪK���뵽����������
				MetaphAdd(primary, 'K');
				break;
			case 'L':
				// �����һλ��L
				if (in.charAt(current + 1) == 'L') {
					// �����ǰλ���ڵ��ʵ�������λ�Ҵ�ǰһλ��ʼ4����ĸƥ��list56
					// �򣨵������������ĸƥ��list57�򵥴����һλƥ��list58���Ҵӵ�ǰλ�õ�ǰһ����ĸ��ʼ4����ĸƥ��list59
					if (((current == (length - 3)) && stringAt(in, current - 1, 4, list56))
							|| ((stringAt(in, last - 1, 2, list57) || stringAt(in, last, 1, list58))
									&& stringAt(in, current - 1, 4, list59))) {
						// ��ΪL���뵽����������
						MetaphAdd(primary, 'L');
						// ���Ժ�һλ
						current += 2;
						break;
					}
					// ���Ժ�һλ
					current += 2;
				} else
					current += 1;
				// ��ΪL���뵽����������
				MetaphAdd(primary, 'L');
				break;
			case 'M':
				// �����ǰһ����ĸ��ʼ3����ĸƥ��list60�Һ�һ����ĸ�ǵ��ʵĽ����������ڶ�����ĸ��ʼ������ĸƥ��list60
				// ���һ����ĸ��M
				if ((stringAt(in, current - 1, 3, list60)
						&& (((current + 1) == last) || stringAt(in, current + 2, 2, list61)))
						|| (in.charAt(current + 1) == 'M'))
					// ���Ժ�һλ
					current += 2;
				else
					current += 1;
				// ��ΪM���뵽����������
				MetaphAdd(primary, 'M');
				break;
			case 'N':
				// �����һ����ĸ��N
				if (in.charAt(current + 1) == 'N')
					// ���Ժ�һλ
					current += 2;
				else
					current += 1;
				// ��ΪN���뵽����������
				MetaphAdd(primary, 'N');
				break;
			// �����˺ŵĴ�д������ĸN��C/C++/Java source code	"\u00D1"��
			case '\u00D1':
				current += 1;
				// ��ΪN���뵽����������
				MetaphAdd(primary, 'N');
				break;
			case 'P':
				// �����һλ��N
				if (in.charAt(current + 1) == 'N') {
					// ��ΪF���뵽����������
					MetaphAdd(primary, 'F');
					// ���Ժ�һλ
					current += 2;
					break;
				}
				// �����һλƥ��list62
				if (stringAt(in, current + 1, 1, list62))
					// ���Ժ�һλ
					current += 2;
				else
					current += 1;
				// ��ΪP���뵽����������
				MetaphAdd(primary, 'P');
				break;
			case 'Q':
				// �����һλ��Q
				if (in.charAt(current + 1) == 'Q')
					// ���Ժ�һλ
					current += 2;
				else
					current += 1;
				// ��ΪK���뵽����������
				MetaphAdd(primary, 'K');
				break;
			case 'R':
				// �����ǰ�����һλ�Ҳ���˹�����ն�������ǰ��������ĸƥ��list63�Ҵ���ǰ���ĸ���ĸ��ʼ������ĸƥ��list64
				if ((current == last) && !isSlavoGermaic && stringAt(in, current - 2, 2, list63)
						&& !stringAt(in, current - 4, 2, list64)) {
					// MetaphAdd(primary, "");
				} else
					// ��ΪR���뵽����������
					MetaphAdd(primary, 'R');
				// �����һλ��R
				if (in.charAt(current + 1) == 'R')
					// ���Ժ�һλ
					current += 2;
				else
					current += 1;
				break;
			case 'S':
				// �����ǰһ����ĸ��ʼ3����ĸƥ��list65
				if (stringAt(in, current - 1, 3, list65)) {
					current += 1;
					break;
				}
				// ���λ�ڴ����Ҵӵ�ǰ��ĸ��ʼ5����ĸƥ��list66
				if ((current == 0) && stringAt(in, current, 5, list66)) {
					// ��ΪX���뵽����������
					MetaphAdd(primary, 'X');
					current += 1;
					break;
				}
				// ����ӵ�ǰ��ĸ��ʼ2����ĸƥ��list67
				if (stringAt(in, current, 2, list67)) {
					// ����Ӻ�һ����ĸ��ʼ4����ĸƥ��list68
					if (stringAt(in, current + 1, 4, list68))
						// ��ΪS���뵽����������
						MetaphAdd(primary, 'S');
					else
						// ��ΪX���뵽����������
						MetaphAdd(primary, 'X');
					// ���Ժ�һλ
					current += 2;
					break;
				}
				// ����ӵ�ǰ��ĸ��ʼ3����ĸƥ��list69��ӵ�ǰ��ĸ��ʼ4����ĸƥ��list70
				if (stringAt(in, current, 3, list69) || stringAt(in, current, 4, list70)) {
					// ��ΪS���뵽����������
					MetaphAdd(primary, 'S');
					// ���Ժ���λ
					current += 3;
					break;
				}
				// ���(λ�ڴ����Һ�һ����ĸƥ��list71)���һ����ĸƥ��list72
				if (((current == 0) && stringAt(in, current + 1, 1, list71)) || stringAt(in, current + 1, 1, list72)) {
					// ��ΪS���뵽����������
					MetaphAdd(primary, 'S');
					// ��һ����ĸƥ��list73
					if (stringAt(in, current + 1, 1, list73))
						// ���Ժ�һλ
						current += 2;
					else
						current += 1;
					break;
				}
				// ����ӵ�ǰ��ĸ��ʼ2����ĸƥ��list74
				if (stringAt(in, current, 2, list74)) {
					// ��������2����ĸƥ��H
					if (in.charAt(current + 2) == 'H')
						// ����������3����ĸ��ʼ2����ĸƥ��list75
						if (stringAt(in, current + 3, 2, list75)) {
							// ����������3����ĸ��ʼ2����ĸƥ��list76
							if (stringAt(in, current + 3, 2, list76)) {
								// ��ΪX���뵽����������
								MetaphAdd(primary, "X");
							} else {
								// ��ΪSK���뵽����������
								MetaphAdd(primary, "SK");
							}
							// ���Ժ���λ
							current += 3;
							break;
						} else {
							// ��ΪX���뵽����������
							MetaphAdd(primary, 'X');
							// ���Ժ���λ
							current += 3;
							break;
						}
					// ��������2����ĸƥ��list77
					if (stringAt(in, current + 2, 1, list77)) {
						// ��ΪS���뵽����������
						MetaphAdd(primary, 'S');
						// ���Ժ���λ
						current += 3;
						break;
					}
					// ��ΪSK���뵽����������
					MetaphAdd(primary, "SK");
					// ���Ժ���λ
					current += 3;
					break;
				}
				// �����ǰ�����һλ��ǰ������ĸƥ��list78
				if ((current == last) && stringAt(in, current - 2, 2, list78)) {
					// MetaphAdd(primary, "");
				} else
					// ��ΪS���뵽����������
					MetaphAdd(primary, 'S');
				// �����һλƥ��list79
				if (stringAt(in, current + 1, 1, list79))
					// ���Ժ�һλ
					current += 2;
				else
					current += 1;
				break;
			case 'T':
				// ����ӵ�ǰ��ĸ��ʼ4����ĸƥ��list80
				if (stringAt(in, current, 4, list80)) {
					// ��ΪX���뵽����������
					MetaphAdd(primary, 'X');
					// ���Ժ���λ
					current += 3;
					break;
				}
				// ����ӵ�ǰ��ĸ��ʼ3����ĸƥ��list81
				if (stringAt(in, current, 3, list81)) {
					// ��ΪX���뵽����������
					MetaphAdd(primary, 'X');
					// ���Ժ���λ
					current += 3;
					break;
				}
				// ����ӵ�ǰ��ĸ��ʼ2����ĸƥ��list82��ӵ�ǰ��ĸ��ʼ3����ĸƥ��list83
				if (stringAt(in, current, 2, list82) || stringAt(in, current, 3, list83)) {
					// ����������2����ĸ��ʼ������ĸƥ��list84�򵥴ʵ�ǰ4����ĸƥ��list85�򵥴ʵ�ǰ3����ĸƥ��list86
					if (stringAt(in, (current + 2), 2, list84) || stringAt(in, 0, 4, list85)
							|| stringAt(in, 0, 3, list86)) {
						// ��ΪT���뵽����������
						MetaphAdd(primary, 'T');
					} else {
						// ��Ϊ0(th)���뵽����������
						MetaphAdd(primary, '0');
					}
					// ���Ժ�һλ
					current += 2;
					break;
				}
				// �����һ����ĸƥ��list87
				if (stringAt(in, current + 1, 1, list87)) {
					// ���Ժ�һλ
					current += 2;
				} else
					current += 1;
				// ��ΪT���뵽����������
				MetaphAdd(primary, 'T');
				break;
			case 'V':
				// �����һ����ĸ��V
				if (in.charAt(current + 1) == 'V')
					// ���Ժ�һλ
					current += 2;
				else
					current += 1;
				// ��ΪF���뵽����������
				MetaphAdd(primary, 'F');
				break;
			case 'W':
				// ����ӵ�ǰ��ĸ��ʼ2����ĸƥ��list88
				if (stringAt(in, current, 2, list88)) {
					// ��ΪR���뵽����������
					MetaphAdd(primary, 'R');
					// ���Ժ�һλ
					current += 2;
					break;
				}
				// ���λ�ڴ����ң���һ����ĸ��Ԫ����ӵ�ǰ��ĸ��ʼ2����ĸƥ��list89��
				if ((current == 0) && (isVowel(in, current + 1, length) || stringAt(in, current, 2, list89))) {
					// ��ΪA���뵽����������
					MetaphAdd(primary, 'A');
				}
				// �������ǰ�����һλ��ǰһλ��Ԫ�������ǰһ����ĸ��ʼ5����ĸƥ��list90�򵥴�ǰ��λƥ��list91
				if (((current == last) && isVowel(in, current - 1, length)) || stringAt(in, current - 1, 5, list90)
						|| stringAt(in, 0, 3, list91)) {
					// ��ΪF���뵽����������
					MetaphAdd(primary, 'F');
					current += 1;
					break;
				}
				// ����ӵ�ǰ��ĸ��ʼ4����ĸƥ��list92
				if (stringAt(in, current, 4, list92)) {
					// ��ΪTS���뵽����������
					MetaphAdd(primary, "TS");
					// ���Ժ���λ
					current += 4;
					break;
				}
				current += 1;
				break;
			case 'X':
				// �����ǰ���ǣ���β�ң�ǰ������ĸƥ��list93��ǰ������ĸƥ��list94����
				if (!((current == last)
						&& (stringAt(in, current - 3, 3, list93) || stringAt(in, current - 2, 2, list94))))
					// ��ΪKS���뵽����������
					MetaphAdd(primary, "KS");
				// �����һ����ĸƥ��list95
				if (stringAt(in, current + 1, 1, list95))
					// ���Ժ�һλ
					current += 2;
				else
					current += 1;
				break;
			case 'Z':
				// �����һ����ĸ��H
				if (in.charAt(current + 1) == 'H') {
					// ��ΪJ���뵽����������
					MetaphAdd(primary, 'J');
					// ���Ժ�һλ
					current += 2;
					break;
				} else {
					// ��ΪS���뵽����������
					MetaphAdd(primary, 'S');
				}
				// �����һλ��Z
				if (in.charAt(current + 1) == 'Z')
					// ���Ժ�һλ
					current += 2;
				else
					current += 1;
				break;
			default:
				current += 1;
			}
		}
		// ������������
		return primary.toString();
	}

	/**
	 * @see com.swabunga.spell.engine.Transformator#getReplaceList()
	 */
	public char[] getReplaceList() {
		return replaceList;
	}
}
