def output = new File(basedir, 'target/generated-text/message.txt')

assert output.isFile()
assert output.getText('UTF-8') == "Message: Café — 世界${System.lineSeparator()}"
