def lineSeparator = System.lineSeparator()
def output = new File(basedir, 'target/generated-text/exampleA-1.txt').getText('UTF-8')

assert output == "Absolute directory: Oz-Ware Purchase Invoice for Dorothy Gale${lineSeparator}"
