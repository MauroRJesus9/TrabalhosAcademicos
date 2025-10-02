using HabitAqui.Data;
using HabitAqui.Models;
using HabitAqui.ViewModels;
using HabitAqui.ViewModels.UsersAnonimos;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Identity;
using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.Mvc.Rendering;
using Microsoft.EntityFrameworkCore;

namespace HabitAqui.Controllers
{

    public class UserAnonimoController : Controller
    {

        private readonly ApplicationDbContext _context;
        private readonly UserManager<Utilizadores> _userManager;
        private readonly Habitacoes _habitacoes;
        private readonly Categorias _categorias;


        public UserAnonimoController(ApplicationDbContext context, UserManager<Utilizadores> userManager)
        {
            _context = context;
            _userManager = userManager;
        }

        //Adicionar o registo de uma habitação; 


        [HttpGet]
        public IActionResult Create()
        {
            var categorias = _context.Categorias.Select(c => c.Nome).ToList();
            ViewBag.Categorias = new SelectList(categorias);

            return View();
        }

        [HttpPost]
        [ValidateAntiForgeryToken]
        public async Task<IActionResult> Create(UserAnonimoViewModel model)
        {

            //if (ModelState.IsValid){

            var auxHabitacao = new Habitacoes
            {
                Nome = model.Habitacao.Nome,
                Descricao = model.Habitacao.Descricao,
                DataInicio = model.Habitacao.DataInicio,
                DataFim = model.Habitacao.DataFim,
                Localidade = model.Habitacao.Localidade,
                Preco = model.Habitacao.Preco,
                CategoriaId = _context.Categorias.Where(c => c.Nome.Equals(model.Habitacao.Categoria.Nome)).Select(c => c.Id).FirstOrDefault(),
                Categoria = _context.Categorias.Where(c => c.Nome.Equals(model.Habitacao.Categoria.Nome)).FirstOrDefault(),
                PeriodoMinimoArrendamento = model.Habitacao.PeriodoMinimoArrendamento,
                Estado = "Livre",
                Ativo = true,
                EmpresaId = GetEmpresaId(),
                Empresa = _context.Empresa.Find(GetEmpresaId())
            };

            var resultadoRegistro = await _context.Habitacoes.AddAsync(auxHabitacao);


            if (resultadoRegistro.State == EntityState.Added)
            {

                var categoriaExist = _context.Categorias.Where(c => c.Nome.Equals(model.Habitacao.Categoria.Nome)).FirstOrDefault();

                if (categoriaExist != null)
                {

                    auxHabitacao.CategoriaId = _context.Categorias.Where(c => c.Nome.Equals(model.Habitacao.Categoria.Nome)).Select(c => c.Id).FirstOrDefault();
                    auxHabitacao.Categoria = categoriaExist;
                    _context.SaveChanges();


                    return RedirectToAction(nameof(Index));
                }
            }
            else
                TempData["ErroMensagem"] = "Erro ao criar uma habitação.";
            //}

            var categorias = _context.Categorias.Select(c => c.Nome).ToList();
            ViewBag.Categorias = new SelectList(categorias);

            return View(model);

        }


        //Listar os registos das habitações - com filtros(categoria, estado) e com ordenação;

        public IActionResult Index(UserAnonimoViewModel model)
        {

            var habitacoesQuery = _context.Habitacoes.Include(h => h.Categoria).AsQueryable();

            // Aplicar filtros
            if (!string.IsNullOrEmpty(model.CategoriaFiltro))
            {
                habitacoesQuery = habitacoesQuery.Where(h => h.Categoria.Nome == model.CategoriaFiltro);
            }

            if (!string.IsNullOrEmpty(model.EstadoFiltro))
            {
                habitacoesQuery = habitacoesQuery.Where(h => h.Estado == model.EstadoFiltro);
            }

            if (!string.IsNullOrEmpty(model.LocalidadeFiltro))
            {
                habitacoesQuery = habitacoesQuery.Where(h => h.Localidade == model.LocalidadeFiltro);
            }
            if (!string.IsNullOrEmpty(model.PeriodoMinimoArrendamentoFiltro))
            {
                habitacoesQuery = habitacoesQuery.Where(h => h.PeriodoMinimoArrendamento.ToString() == model.PeriodoMinimoArrendamentoFiltro);
            }

            // Aplicar ordenação
            if (!string.IsNullOrEmpty(model.OrdenarPor))
            {

                habitacoesQuery = OrdenarHabitacoes(habitacoesQuery, model.OrdenarPor, model.OrdemAscendente);

            }

            var habitacoes = habitacoesQuery.ToList();

            // Preencher ViewBag para usar no filtro e ordenação da View
            ViewBag.Categorias = new SelectList(_context.Categorias.Select(c => c.Nome).ToList());
            ViewBag.Estados = new SelectList(_context.Habitacoes.Select(h => h.Estado).Distinct().ToList());
            ViewBag.Localidade = new SelectList(_context.Habitacoes.Select(h => h.Localidade).Distinct().ToList());
            ViewBag.PeriodoMinimoArrendamento = new SelectList(_context.Habitacoes.Select(h => h.PeriodoMinimoArrendamento).Distinct().ToList());
            model.habitacoes = habitacoes;

            return View(model);
        }

        private IQueryable<Habitacoes> OrdenarHabitacoes(IQueryable<Habitacoes> habitacoesQuery, string ordenarPor, bool ordemAscendente)
        {
            switch (ordenarPor)
            {
                case "Nome":
                    return ordemAscendente ? habitacoesQuery.OrderBy(h => h.Nome) : habitacoesQuery.OrderByDescending(h => h.Nome);
                case "DataInicio":
                    return ordemAscendente ? habitacoesQuery.OrderBy(h => h.DataInicio) : habitacoesQuery.OrderByDescending(h => h.DataInicio);
                case "DataFim":
                    return ordemAscendente ? habitacoesQuery.OrderBy(h => h.DataFim) : habitacoesQuery.OrderByDescending(h => h.DataFim);
                case "Localidade":
                    return ordemAscendente ? habitacoesQuery.OrderBy(h => h.Localidade) : habitacoesQuery.OrderByDescending(h => h.Localidade);
                case "Preco":
                    return ordemAscendente ? habitacoesQuery.OrderBy(h => h.Preco) : habitacoesQuery.OrderByDescending(h => h.Preco);
                // Adicionar mais casos conforme necessário para outros campos
                default:
                    return habitacoesQuery;
            }
        }



        //Editar o registo de uma habitação;

        [HttpGet]
        public IActionResult Edit(int habitacaoId)
        {
            var habitacao = _context.Habitacoes
                .Include(h => h.Categoria)
                .FirstOrDefault(h => h.Id == habitacaoId);

            if (habitacao == null)
                return NotFound();

            var categorias = _context.Categorias.Select(c => c.Nome).ToList();
            ViewBag.Categorias = new SelectList(categorias);

            var model = new UserAnonimoViewModel
            {
                Habitacao = habitacao

            };

            return View(model);
        }

        [HttpPost]
        [ValidateAntiForgeryToken]
        public async Task<IActionResult> Edit(int? habitacaoId, UserAnonimoViewModel model)
        {

            //if (ModelState.IsValid){
            try
            {

                var hab = await _context.Habitacoes.FindAsync(habitacaoId);

                if (hab == null)
                    return NotFound();

                hab.Nome = model.Habitacao.Nome;
                hab.Descricao = model.Habitacao.Descricao;
                hab.DataInicio = model.Habitacao.DataInicio;
                hab.DataFim = model.Habitacao.DataFim;
                hab.Localidade = model.Habitacao.Localidade;
                hab.Preco = model.Habitacao.Preco;
                hab.CategoriaId = _context.Categorias.Where(c => c.Nome.Equals(model.Habitacao.Categoria.Nome)).Select(c => c.Id).FirstOrDefault();
                hab.Categoria = _context.Categorias.Where(c => c.Nome.Equals(model.Habitacao.Categoria.Nome)).FirstOrDefault();
                hab.PeriodoMinimoArrendamento = model.Habitacao.PeriodoMinimoArrendamento;

                _context.Habitacoes.Update(hab);
                await _context.SaveChangesAsync();

                return RedirectToAction(nameof(Index));
            }
            catch (DbUpdateConcurrencyException)
            {
                TempData["ErroMensagem"] = "Erro ao salvar as alterações.";
            }
            //}

            // Se houver algum erro, recarregue a view com os dados existentes
            var categorias = _context.Categorias.Select(c => c.Nome).ToList();
            ViewBag.Categorias = new SelectList(categorias);

            return View(model);
        }


        //Ativar ou inativar o registo de uma habitação.
        [HttpPost]
        public async Task<IActionResult> AtivarInativar(int habitacaoId)
        {

            var habitacao = await _context.Habitacoes.FindAsync(habitacaoId);

            if (habitacao == null)
                return NotFound();

            // Inverte o estado de Ativo da habitação
            habitacao.Ativo = !habitacao.Ativo;

            _context.Update(habitacao);
            await _context.SaveChangesAsync();


            return RedirectToAction(nameof(Index));
        }

        [HttpPost]
        public async Task<IActionResult> Delete(int habitacaoId)
        {

            var habitacao = await _context.Habitacoes.FindAsync(habitacaoId);

            if (habitacao == null)
                return NotFound();

            // Verifica se existem arrendamentos associados a esta habitação
            var arrendamentosExistem = _context.Arrendamentos.Any(a => a.HabitacoesId == habitacaoId);

            if (arrendamentosExistem)
                TempData["ErroMensagem"] = "Não é possível apagar a habitação porque existem arrendamentos associados.";

            else
            {
                _context.Habitacoes.Remove(habitacao);
                await _context.SaveChangesAsync();
            }

            return RedirectToAction(nameof(Index));
        }

        private int? GetEmpresaId()
        {
            var funcionarioId = _userManager.GetUserId(User);
            var funcionario = _userManager.FindByIdAsync(funcionarioId).Result;

            return funcionario.EmpresaId;
        }
    }



}
