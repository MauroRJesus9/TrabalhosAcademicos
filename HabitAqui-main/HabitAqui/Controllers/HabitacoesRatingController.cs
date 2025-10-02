using HabitAqui.Data;
using HabitAqui.Models;
using HabitAqui.ViewModels;
using Microsoft.AspNetCore.Identity;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;

namespace HabitAqui.Controllers
{

    public class HabitacoesRatingController : Controller{

        private readonly ApplicationDbContext _context;
        private readonly UserManager<Utilizadores> _userManager;

        public HabitacoesRatingController(ApplicationDbContext context, UserManager<Utilizadores> userManager){
            _context = context;
            _userManager = userManager;
        }

        public IActionResult Index()
        {

            var userId = _userManager.GetUserId(User);

            var ratings = _context.HabitacoesRating
                .Include(r => r.Habitacoes)
                .Where(r => r.UtilizadoresId == userId)
                .Select(r => new HabitacoesRatingViewModel
                {
                    HabitacaoRatingId = r.ID,
                    Comentario = r.Comentario,
                    Habitacao = r.Habitacoes.Nome,
                    Arrendamentos = _context.Arrendamentos.Where(a => a.HabitacoesId == r.HabitacoesId).Where(a => a.UtilizadoresId == userId).FirstOrDefault()
                    
                })
                .ToList();

            return View(ratings);
        }


        public IActionResult Create(int habitacaoId)
        {
            var userId = _userManager.GetUserId(User);

            // Verifique se o utilizador já avaliou esta habitação
            var existingRating = _context.HabitacoesRating
                .FirstOrDefault(r => r.HabitacoesId == habitacaoId && r.UtilizadoresId == userId);

            if (existingRating != null){
                // O utilizador já avaliou esta habitação, redirecione para uma página de erro ou faça algo apropriado
                return RedirectToAction("Index", "Habitacoes");
            }

            // Se o utilizador ainda não avaliou, crie um novo objeto HabitacoesRating
            var viewModel = new HabitacoesRatingViewModel {
                HabitacaoRatingId = habitacaoId
            };

            return View(viewModel);
        }

        [HttpPost]
        [ValidateAntiForgeryToken]
        public async Task<IActionResult> Create(HabitacoesRatingViewModel model)
        {
            if (ModelState.IsValid){
                // Mapeie os dados do ViewModel para o modelo HabitacoesRating
                var habitacoesRating = new HabitacoesRating
                {
                    Comentario = model.Comentario,
                    HabitacoesId = model.HabitacaoRatingId,
                    UtilizadoresId = _userManager.GetUserId(User),            
                    
                };

                // Adicione a nova avaliação ao contexto e salve as alterações
                _context.HabitacoesRating.Add(habitacoesRating);
                await _context.SaveChangesAsync();

                // Redirecione para a página de detalhes da habitação ou para onde desejar
                return RedirectToAction("Details", "Habitacoes", new { id = model.HabitacaoRatingId });
            }

            // Se houver erros de validação, retorne para a View com os dados do modelo
            return View(model);
        }
    }
}