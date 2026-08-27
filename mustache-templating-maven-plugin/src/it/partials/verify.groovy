def lineSeparator = System.lineSeparator()
def expected = """Test-3: Test mustache file: receipt is Oz-Ware Purchase Invoice for Dorothy Gale${lineSeparator}Partial#1: Test-3a: Bill-to: 123 Tornado Alley&#10;Suite 16&#10;${lineSeparator}${lineSeparator}Partial#2: Test-3b: - Ship-to: 123 Tornado Alley&#10;Suite 16&#10;${lineSeparator}${lineSeparator}"""

assert new File(basedir, 'target/generated-text/exampleA-1.txt').getText('UTF-8') == expected

