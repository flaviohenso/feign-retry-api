package com.example.feignretryapi.domain.gateway;

import com.example.feignretryapi.domain.entity.Product;

import java.util.List;
import java.util.Optional;

/**
 * Interface de Gateway para operações com Produtos.
 * Define o contrato para comunicação com a API externa.
 */
public interface ProductGateway {

    /**
     * Busca todos os produtos da API externa.
     *
     * @return Lista de produtos
     */
    List<Product> findAll();

    /**
     * Busca um produto pelo ID.
     *
     * @param id Identificador do produto
     * @return Optional contendo o produto ou vazio se não encontrado
     */
    Optional<Product> findById(String id);

    /**
     * Busca produtos por categoria.
     *
     * @param category Categoria dos produtos
     * @return Lista de produtos da categoria
     */
    List<Product> findByCategory(String category);
}
