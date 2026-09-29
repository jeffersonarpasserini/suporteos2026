package com.curso.suporteos.domain;

public final class DocumentoFiscal {

    private DocumentoFiscal() {
    }

    public static boolean cpfValido(String cpf) {
        if (cpf == null || !cpf.matches("\\d{11}") || digitosRepetidos(cpf)) {
            return false;
        }

        int primeiro = calcularDigito(cpf.substring(0, 9), 10);
        int segundo = calcularDigito(cpf.substring(0, 9) + primeiro, 11);
        return cpf.equals(cpf.substring(0, 9) + primeiro + segundo);
    }

    public static boolean cnpjValido(String cnpj) {
        if (cnpj == null || !cnpj.matches("\\d{14}") || digitosRepetidos(cnpj)) {
            return false;
        }

        int primeiro = calcularDigitoCnpj(cnpj.substring(0, 12));
        int segundo = calcularDigitoCnpj(cnpj.substring(0, 12) + primeiro);
        return cnpj.equals(cnpj.substring(0, 12) + primeiro + segundo);
    }

    private static int calcularDigito(String base, int pesoInicial) {
        int soma = 0;
        for (int indice = 0; indice < base.length(); indice++) {
            soma += Character.getNumericValue(base.charAt(indice)) * (pesoInicial - indice);
        }
        int resto = soma % 11;
        return resto < 2 ? 0 : 11 - resto;
    }

    private static int calcularDigitoCnpj(String base) {
        int peso = base.length() - 7;
        int soma = 0;
        for (int indice = 0; indice < base.length(); indice++) {
            soma += Character.getNumericValue(base.charAt(indice)) * peso;
            peso--;
            if (peso == 1) {
                peso = 9;
            }
        }
        int resto = soma % 11;
        return resto < 2 ? 0 : 11 - resto;
    }

    private static boolean digitosRepetidos(String valor) {
        return valor.chars().allMatch(digito -> digito == valor.charAt(0));
    }
}
