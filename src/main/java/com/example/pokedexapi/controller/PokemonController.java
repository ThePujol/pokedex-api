package com.example.pokedexapi.controller;

import com.example.pokedexapi.dto.DashboardStats;
import com.example.pokedexapi.dto.GenericApiResponse;
import com.example.pokedexapi.dto.PokemonRequest;
import com.example.pokedexapi.model.Pokemon;
import com.example.pokedexapi.repository.PokemonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.Objects;
import java.util.Collections;
import java.util.stream.Collectors;

@RestController
@RequestMapping // (Prefixo /api)
public class PokemonController {

    @Autowired
    private PokemonRepository pokemonRepository;

    // --- Endpoint do Dashboard (Etapa 3 do App) ---
    // GET /api/dashboard/stats
    @GetMapping("/dashboard/stats")
    public ResponseEntity<DashboardStats> getDashboardStats() {
        long count = pokemonRepository.count();
        
        // Lógica simples para Top 3 (em um app real, isso seria mais complexo)
        List<String> topTipos = pokemonRepository.findAll().stream()
                .map(Pokemon::getTipo)
                .distinct()
                .limit(3)
                .collect(Collectors.toList());

        List<String> topHabilidades = pokemonRepository.findAll().stream()
                .flatMap(p -> p.getHabilidades().stream())
                .distinct()
                .limit(3)
                .collect(Collectors.toList());

        DashboardStats stats = new DashboardStats(count, topTipos, topHabilidades);
        return ResponseEntity.ok(stats);
    }

    // --- Endpoint de Cadastro (Etapa 4 do App) ---
    // POST /api/pokemon
    @PostMapping("/pokemon")
    public ResponseEntity<GenericApiResponse> createPokemon(@RequestBody PokemonRequest request) {
        // Verifica se o Pokémon já existe [cite: 3912]
        if (pokemonRepository.findByNome(request.getNome()).isPresent()) {
            return ResponseEntity.ok(
                new GenericApiResponse(false, "Erro: Este Pokémon já existe.")
            );
        }
        
        Pokemon pokemon = new Pokemon();
        pokemon.setNome(request.getNome());
        pokemon.setTipo(request.getTipo());
        pokemon.setHabilidades(request.getHabilidades());
        pokemon.setUsuarioLogin(request.getUsuarioLogin());
        pokemonRepository.save(pokemon);

        return ResponseEntity.ok(
            new GenericApiResponse(true, "Pokémon cadastrado com sucesso!")
        );
    }

    // --- Endpoint Listar Todos (Etapa 5 do App) ---
    // GET /api/pokemon
    @GetMapping("/pokemon")
    public ResponseEntity<List<Pokemon>> getAllPokemon() {
        return ResponseEntity.ok(pokemonRepository.findAll());
    }

    // --- Endpoints de Detalhes (Etapa 6 do App) ---
    // PUT /api/pokemon/{id}
    @PutMapping("/pokemon/{id}")
    public ResponseEntity<GenericApiResponse> updatePokemon(
            @PathVariable Integer id, @RequestBody Pokemon pokemonAtualizado) {
        
        return pokemonRepository.findById(id).map(pokemon -> {
            
            pokemon.setTipo(pokemonAtualizado.getTipo());
            pokemon.setHabilidades(pokemonAtualizado.getHabilidades());
    
            pokemonRepository.save(pokemon);
            return ResponseEntity.ok(
                new GenericApiResponse(true, "Pokémon atualizado com sucesso.")
            );
        }).orElse(ResponseEntity.ok(
            new GenericApiResponse(false, "Erro: Pokémon não encontrado.")
        ));
    }

    // DELETE /api/pokemon/{id}
    @DeleteMapping("/pokemon/{id}")
    public ResponseEntity<GenericApiResponse> deletePokemon(@PathVariable Integer id) {
        if (pokemonRepository.existsById(id)) {
            pokemonRepository.deleteById(id);
            return ResponseEntity.ok(
                new GenericApiResponse(true, "Pokémon excluído com sucesso.")
            );
        }
        return ResponseEntity.ok(
            new GenericApiResponse(false, "Erro: Pokémon não encontrado.")
        );
    }

    // --- Endpoints de Busca (Etapa 7 e 8 do App) ---
    // GET /api/pokemon/tipo/{tipo}
    @GetMapping("/pokemon/tipo/{tipo}")
    public ResponseEntity<List<Pokemon>> getPokemonByType(@PathVariable String tipo) {
        return ResponseEntity.ok(pokemonRepository.findByTipoContainingIgnoreCase(tipo));
    }

    // GET /api/pokemon/habilidade/{habilidade}
    @GetMapping("/pokemon/habilidade/{habilidade}")
    public ResponseEntity<List<Pokemon>> getPokemonByAbility(@PathVariable String habilidade) {
        return ResponseEntity.ok(pokemonRepository.findByHabilidade(habilidade));
    }

     // --- Endpoints to get all distinct tipos and habilidades ---
    // GET /api/pokemon/tipos
    @GetMapping("/pokemon/tipos")
    public ResponseEntity<List<String>> getAllTipos() {
        List<String> tipos = pokemonRepository.findAll().stream()
                .map(Pokemon::getTipo)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        return ResponseEntity.ok(tipos);
    }

    // GET /api/pokemon/habilidades
    @GetMapping("/pokemon/habilidades")
    public ResponseEntity<List<String>> getAllHabilidades() {
        List<String> habilidades = pokemonRepository.findAll().stream()
                .flatMap(p -> Optional.ofNullable(p.getHabilidades()).orElse(Collections.emptyList()).stream())
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        return ResponseEntity.ok(habilidades);
    }
}
