using System.ComponentModel.DataAnnotations;

namespace HabitAqui.ViewModels.Clientes
{
    public class ClienteAddViewModel{

        [EmailAddress]
        public string Email { get; set; }

        [DataType(DataType.Password)]
        public string Password { get; set; }

        public string Role { get; set; }

        public bool Ativo { get; set; }

    }
}
